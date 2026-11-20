package com.example.cw.cart.model;

import lombok.Getter;

@Getter
public class UpdateCartItemCommand {

    private final Integer userId;
    private final Integer productId;
    private final Integer quantity;

    public UpdateCartItemCommand(Integer userId, Integer productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}