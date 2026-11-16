package com.example.cw.cart.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartDTO {

    private Integer id;

    private Integer userId;

    private List<CartItemDTO> items = new ArrayList<>();

    private BigDecimal totalPrice = BigDecimal.ZERO;
}