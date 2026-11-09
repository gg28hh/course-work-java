package com.example.cw.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table
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
}
