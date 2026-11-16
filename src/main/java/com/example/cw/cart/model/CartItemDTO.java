package com.example.cw.cart.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {

    private Integer id;

    private Integer productId;

    private String productName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}