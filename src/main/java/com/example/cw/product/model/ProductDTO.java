package com.example.cw.product.model;

import lombok.Data;

@Data
public class ProductDTO {
    private  Integer id;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;

}
