package com.example.cw.order;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.CheckoutOrderCommand;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
class CheckoutRollbackIntegrationTest {

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
    void failedCheckoutRollsBackStockAndKeepsCart() {

        Integer userId = 91234572;

        TestScenario scenario = transactionTemplate.execute(status -> {

            assertTrue(cartRepository.findByUserId(userId).isEmpty());
            assertTrue(orderRepository.findByUserId(userId).isEmpty());

            Product firstProduct = new Product();

            firstProduct.setName("Rollback Test Product A");
            firstProduct.setDescription(
                    "First product for checkout rollback testing."
            );
            firstProduct.setPrice(10.00);
            firstProduct.setStockQuantity(5);

            entityManager.persist(firstProduct);

            Product secondProduct = new Product();

            secondProduct.setName("Rollback Test Product B");
            secondProduct.setDescription(
                    "Second product for checkout rollback testing."
            );
            secondProduct.setPrice(20.00);
            secondProduct.setStockQuantity(0);

            entityManager.persist(secondProduct);

            Cart cart = new Cart(userId);

            entityManager.persist(cart);

            entityManager.persist(
                    new CartItem(cart, firstProduct, 2)
            );

            entityManager.persist(
                    new CartItem(cart, secondProduct, 1)
            );

            entityManager.flush();

            return new TestScenario(
                    userId,
                    cart.getId(),
                    firstProduct.getId(),
                    secondProduct.getId()
            );
        });

        assertNotNull(scenario);

        try {

            assertThrows(IllegalArgumentException.class, () ->
                    checkoutOrderService.execute(
                            new CheckoutOrderCommand(userId)
                    )
            );

            transactionTemplate.executeWithoutResult(status -> {

                Integer firstStock = jdbcTemplate.queryForObject(
                        "SELECT stock_quantity FROM product WHERE id = ?",
                        Integer.class,
                        scenario.firstProductId()
                );

                Integer secondStock = jdbcTemplate.queryForObject(
                        "SELECT stock_quantity FROM product WHERE id = ?",
                        Integer.class,
                        scenario.secondProductId()
                );

                Integer cartItems = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?",
                        Integer.class,
                        scenario.cartId()
                );

                assertEquals(5, firstStock);
                assertEquals(0, secondStock);
                assertEquals(2, cartItems);

                assertTrue(
                        orderRepository.findByUserId(userId).isEmpty()
                );
            });

        } finally {

            transactionTemplate.executeWithoutResult(status -> {

                jdbcTemplate.update(
                        "DELETE FROM order_items WHERE order_id IN " +
                                "(SELECT id FROM customer_orders WHERE user_id = ?)",
                        scenario.userId()
                );

                jdbcTemplate.update(
                        "DELETE FROM customer_orders WHERE user_id = ?",
                        scenario.userId()
                );

                jdbcTemplate.update(
                        "DELETE FROM cart_items WHERE cart_id = ?",
                        scenario.cartId()
                );

                jdbcTemplate.update(
                        "DELETE FROM carts WHERE id = ?",
                        scenario.cartId()
                );

                jdbcTemplate.update(
                        "DELETE FROM product WHERE id IN (?, ?)",
                        scenario.firstProductId(),
                        scenario.secondProductId()
                );
            });
        }
    }

    private record TestScenario(
            Integer userId,
            Integer cartId,
            Integer firstProductId,
            Integer secondProductId
    ) {
    }
}