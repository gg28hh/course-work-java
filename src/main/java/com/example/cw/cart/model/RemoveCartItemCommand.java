package com.example.cw.cart.model;

import lombok.Getter;

@Getter
public class RemoveCartItemCommand {

    private final Integer userId;
    private final Integer productId;

    public RemoveCartItemCommand(Integer userId, Integer productId) {
        this.userId = userId;
        this.productId = productId;
    }
}