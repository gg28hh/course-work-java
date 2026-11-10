package com.example.cw.product.model;

import com.example.cw.category.model.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Name is required!")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Description is required!")
    @Size(min = 15, message = "Description must be longer!")
    @Column(name = "description")
    private String description;

    @NotNull(message = "Price is required!")
    @PositiveOrZero(message = "Price cannot be negative!")
    @Column(name = "price")
    private Double price;

    @NotNull(message = "Quantity is required!")
    @PositiveOrZero(message = "Quantity cannot be negative!")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}

