package com.example.cw.cart;

import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.product.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("cart-test")
@EnabledIfEnvironmentVariable(
        named = "CART_TEST_DB_PASSWORD",
        matches = ".+"
)
class CartPersistenceIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSaveAndLoadCartItemFromMySQL() {

        // 1. Create a test product
        Product product = new Product();
        product.setName("MySQL Cart Test Product");
        product.setDescription("Product used for MySQL integration testing.");
        product.setPrice(25.50);
        product.setStockQuantity(10);

        entityManager.persist(product);

        // 2. Create a shopping cart
        Cart cart = new Cart(91234567);

        entityManager.persist(cart);

        // 3. Add a product to the cart
        CartItem cartItem = new CartItem(cart, product, 3);

        entityManager.persist(cartItem);

        // 4. Write changes to MySQL
        entityManager.flush();

        // 5. Clear Hibernate's memory cache
        entityManager.clear();

        // 6. Load the cart from MySQL
        Cart savedCart = entityManager
                .createQuery(
                        "SELECT c FROM Cart c WHERE c.userId = :userId",
                        Cart.class
                )
                .setParameter("userId", 91234567)
                .getSingleResult();

        // 7. Load the cart item from MySQL
        CartItem savedItem = entityManager
                .createQuery(
                        "SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId",
                        CartItem.class
                )
                .setParameter("userId", 91234567)
                .getSingleResult();

        // 8. Verify the saved information
        assertNotNull(savedCart);
        assertNotNull(savedItem);

        assertEquals(91234567, savedCart.getUserId());
        assertEquals(3, savedItem.getQuantity());
        assertEquals(
                "MySQL Cart Test Product",
                savedItem.getProduct().getName()
        );
        assertEquals(
                savedCart.getId(),
                savedItem.getCart().getId()
        );
    }
}