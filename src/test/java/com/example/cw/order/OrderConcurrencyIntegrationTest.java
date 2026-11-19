package com.example.cw.order;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.services.CheckoutOrderService;
import com.example.cw.product.model.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("cart-test")
@EnabledIfEnvironmentVariable(
        named = "CART_TEST_DB_PASSWORD",
        matches = ".+"
)
@Import({
        CheckoutOrderService.class,
        OrderMapper.class
})
class OrderConcurrencyIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CheckoutOrderService checkoutOrderService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void onlyOneCheckoutSucceedsForLastAvailableProduct() throws Exception {

        Integer firstUserId = 91234573;
        Integer secondUserId = 91234574;

        TestScenario scenario = transactionTemplate.execute(status -> {

            assertTrue(cartRepository.findByUserId(firstUserId).isEmpty());
            assertTrue(cartRepository.findByUserId(secondUserId).isEmpty());

            assertTrue(orderRepository.findByUserId(firstUserId).isEmpty());
            assertTrue(orderRepository.findByUserId(secondUserId).isEmpty());

            Product product = new Product();

            product.setName("Concurrency Test Product");
            product.setDescription(
                    "Product for testing simultaneous checkout requests."
            );
            product.setPrice(50.00);
            product.setStockQuantity(1);

            entityManager.persist(product);

            Cart firstCart = new Cart(firstUserId);
            Cart secondCart = new Cart(secondUserId);

            entityManager.persist(firstCart);
            entityManager.persist(secondCart);

            entityManager.persist(
                    new CartItem(firstCart, product, 1)
            );

            entityManager.persist(
                    new CartItem(secondCart, product, 1)
            );

            entityManager.flush();

            return new TestScenario(
                    product.getId(),
                    firstCart.getId(),
                    secondCart.getId()
            );
        });

        assertNotNull(scenario);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {

            try {

                Future<Boolean> firstCheckout = executor.submit(
                        () -> attemptCheckout(
                                firstUserId,
                                readyLatch,
                                startLatch
                        )
                );

                Future<Boolean> secondCheckout = executor.submit(
                        () -> attemptCheckout(
                                secondUserId,
                                readyLatch,
                                startLatch
                        )
                );

                assertTrue(
                        readyLatch.await(10, TimeUnit.SECONDS)
                );

                startLatch.countDown();

                boolean firstSucceeded =
                        firstCheckout.get(30, TimeUnit.SECONDS);

                boolean secondSucceeded =
                        secondCheckout.get(30, TimeUnit.SECONDS);

                int successfulOrders =
                        (firstSucceeded ? 1 : 0)
                                + (secondSucceeded ? 1 : 0);

                assertEquals(1, successfulOrders);

            } finally {

                startLatch.countDown();

                executor.shutdown();

                if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }

            transactionTemplate.executeWithoutResult(status -> {

                Integer stock = jdbcTemplate.queryForObject(
                        "SELECT stock_quantity FROM product WHERE id = ?",
                        Integer.class,
                        scenario.productId()
                );

                Integer ordersCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM customer_orders " +
                                "WHERE user_id IN (?, ?)",
                        Integer.class,
                        firstUserId,
                        secondUserId
                );

                Integer remainingCartItems = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM cart_items " +
                                "WHERE cart_id IN (?, ?)",
                        Integer.class,
                        scenario.firstCartId(),
                        scenario.secondCartId()
                );

                assertEquals(0, stock);
                assertEquals(1, ordersCount);
                assertEquals(1, remainingCartItems);
            });

        } finally {

            transactionTemplate.executeWithoutResult(status -> {

                jdbcTemplate.update(
                        "DELETE FROM order_items WHERE order_id IN " +
                                "(SELECT id FROM customer_orders " +
                                "WHERE user_id IN (?, ?))",
                        firstUserId,
                        secondUserId
                );

                jdbcTemplate.update(
                        "DELETE FROM customer_orders " +
                                "WHERE user_id IN (?, ?)",
                        firstUserId,
                        secondUserId
                );

                jdbcTemplate.update(
                        "DELETE FROM cart_items " +
                                "WHERE cart_id IN (?, ?)",
                        scenario.firstCartId(),
                        scenario.secondCartId()
                );

                jdbcTemplate.update(
                        "DELETE FROM carts WHERE id IN (?, ?)",
                        scenario.firstCartId(),
                        scenario.secondCartId()
                );

                jdbcTemplate.update(
                        "DELETE FROM product WHERE id = ?",
                        scenario.productId()
                );
            });
        }
    }

    private boolean attemptCheckout(
            Integer userId,
            CountDownLatch readyLatch,
            CountDownLatch startLatch) throws InterruptedException {

        readyLatch.countDown();

        if (!startLatch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "Concurrent checkout did not start in time"
            );
        }

        try {

            ResponseEntity<OrderDTO> response =
                    checkoutOrderService.execute(
                            new CheckoutOrderCommand(userId)
                    );

            assertEquals(
                    HttpStatus.CREATED,
                    response.getStatusCode()
            );

            return true;

        } catch (IllegalArgumentException exception) {

            if (!exception.getMessage().contains("Insufficient stock")) {
                throw exception;
            }

            return false;
        }
    }

    private record TestScenario(
            Integer productId,
            Integer firstCartId,
            Integer secondCartId
    ) {
    }
}