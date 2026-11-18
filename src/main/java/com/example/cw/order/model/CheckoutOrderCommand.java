package com.example.cw.order.model;

public class CheckoutOrderCommand {

    private final Integer userId;

    public CheckoutOrderCommand(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}