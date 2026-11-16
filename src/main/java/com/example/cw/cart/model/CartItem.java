package com.example.cw.cart.model;

import com.example.cw.product.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"cart_id", "product_id"}
    )
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    protected CartItem() {
    }

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        setQuantity(quantity);
    }

    public Integer getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException(
                "Quantity must be at least 1"
            );
        }

        this.quantity = quantity;
    }
}