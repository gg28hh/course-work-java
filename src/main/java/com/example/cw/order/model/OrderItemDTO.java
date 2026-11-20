package com.example.cw.order.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Integer id;

    private Integer productId;

    private String productName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}