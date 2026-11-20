package com.example.cw.order;

import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderItem;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.product.model.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
class OrderPersistenceIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSaveAndLoadOrderWithItemsFromMySQL() {

        Product product = new Product();
        product.setName("MySQL Order Test Product");
        product.setDescription(
                "Product used for MySQL order integration testing."
        );
        product.setPrice(25.50);
        product.setStockQuantity(10);

        entityManager.persist(product);

        Order order = new Order(91234568);

        OrderItem orderItem = new OrderItem(
                product,
                3,
                new BigDecimal("25.50")
        );

        order.addItem(orderItem);

        order.setTotalPrice(new BigDecimal("76.50"));

        entityManager.persist(order);
        entityManager.flush();

        Integer orderId = order.getId();
        Integer productId = product.getId();

        entityManager.clear();

        Order savedOrder = entityManager.find(
                Order.class,
                orderId
        );

        assertNotNull(savedOrder);
        assertEquals(91234568, savedOrder.getUserId());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());

        assertEquals(
                0,
                new BigDecimal("76.50")
                        .compareTo(savedOrder.getTotalPrice())
        );

        assertEquals(1, savedOrder.getItems().size());

        OrderItem savedItem = savedOrder.getItems().get(0);

        assertEquals(
                productId,
                savedItem.getProduct().getId()
        );

        assertEquals(
                "MySQL Order Test Product",
                savedItem.getProductName()
        );

        assertEquals(3, savedItem.getQuantity());

        assertEquals(
                0,
                new BigDecimal("25.50")
                        .compareTo(savedItem.getUnitPrice())
        );

        assertEquals(
                0,
                new BigDecimal("76.50")
                        .compareTo(savedItem.getSubtotal())
        );

        assertEquals(
                savedOrder.getId(),
                savedItem.getOrder().getId()
        );
    }
}