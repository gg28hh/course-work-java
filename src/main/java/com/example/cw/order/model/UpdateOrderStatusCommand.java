package com.example.cw.order.model;

public class UpdateOrderStatusCommand {

    private final Integer orderId;
    private final OrderStatus status;

    public UpdateOrderStatusCommand(
            Integer orderId,
            OrderStatus status
    ) {
        this.orderId = orderId;
        this.status = status;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}