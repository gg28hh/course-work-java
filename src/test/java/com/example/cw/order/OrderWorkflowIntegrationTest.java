package com.example.cw.order;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.order.model.UpdateOrderStatusCommand;
import com.example.cw.order.services.CheckoutOrderService;
import com.example.cw.order.services.UpdateOrderStatusService;
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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

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
        UpdateOrderStatusService.class,
        OrderMapper.class
})
class OrderWorkflowIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CheckoutOrderService checkoutOrderService;

    @Autowired
    private UpdateOrderStatusService updateOrderStatusService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void checkoutCreatesOrderAndUpdatesStockAndCart() {

        Integer userId = 91234570;

        Product product = createProduct(userId, 10);

        Integer productId = product.getId();

        prepareCart(userId, product, 3);

        ResponseEntity<OrderDTO> response =
                checkoutOrderService.execute(
                        new CheckoutOrderCommand(userId)
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        OrderDTO dto = response.getBody();

        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(OrderStatus.PENDING, dto.getStatus());

        assertEquals(
                0,
                new BigDecimal("76.50")
                        .compareTo(dto.getTotalPrice())
        );

        Integer orderId = dto.getId();

        entityManager.flush();
        entityManager.clear();

        Product savedProduct = entityManager.find(
                Product.class,
                productId
        );

        assertEquals(7, savedProduct.getStockQuantity());

        Cart savedCart = cartRepository.findByUserId(userId)
                .orElseThrow();

        assertTrue(savedCart.getItems().isEmpty());

        Order savedOrder = orderRepository.findById(orderId)
                .orElseThrow();

        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(
                3,
                savedOrder.getItems().get(0).getQuantity()
        );
    }

    @Test
    void cancellationRestoresStockExactlyOnce() {

        Integer userId = 91234571;

        Product product = createProduct(userId, 5);

        Integer productId = product.getId();

        prepareCart(userId, product, 2);

        ResponseEntity<OrderDTO> checkoutResponse =
                checkoutOrderService.execute(
                        new CheckoutOrderCommand(userId)
                );

        assertEquals(
                HttpStatus.CREATED,
                checkoutResponse.getStatusCode()
        );

        OrderDTO createdOrder = checkoutResponse.getBody();

        assertNotNull(createdOrder);

        Integer orderId = createdOrder.getId();

        entityManager.flush();
        entityManager.clear();

        Product productAfterCheckout = entityManager.find(
                Product.class,
                productId
        );

        assertEquals(3, productAfterCheckout.getStockQuantity());

        ResponseEntity<OrderDTO> cancellationResponse =
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                orderId,
                                OrderStatus.CANCELLED
                        )
                );

        assertEquals(
                HttpStatus.OK,
                cancellationResponse.getStatusCode()
        );

        ResponseEntity<OrderDTO> repeatedCancellation =
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                orderId,
                                OrderStatus.CANCELLED
                        )
                );

        assertEquals(
                HttpStatus.OK,
                repeatedCancellation.getStatusCode()
        );

        entityManager.flush();
        entityManager.clear();

        Product restoredProduct = entityManager.find(
                Product.class,
                productId
        );

        assertEquals(5, restoredProduct.getStockQuantity());

        Order cancelledOrder = orderRepository.findById(orderId)
                .orElseThrow();

        assertEquals(
                OrderStatus.CANCELLED,
                cancelledOrder.getStatus()
        );
    }

    private Product createProduct(Integer userId, Integer stock) {

        Product product = new Product();

        product.setName("Workflow Test Product " + userId);

        product.setDescription(
                "Product for testing the complete order workflow."
        );

        product.setPrice(25.50);
        product.setStockQuantity(stock);

        entityManager.persist(product);

        return product;
    }

    private void prepareCart(
            Integer userId,
            Product product,
            Integer quantity) {

        Cart cart = new Cart(userId);

        entityManager.persist(cart);

        CartItem cartItem = new CartItem(
                cart,
                product,
                quantity
        );

        entityManager.persist(cartItem);

        entityManager.flush();
        entityManager.clear();
    }
}