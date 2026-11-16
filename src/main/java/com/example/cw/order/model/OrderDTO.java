package com.example.cw.order.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDTO {

    private Integer id;

    private Integer userId;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private List<OrderItemDTO> items = new ArrayList<>();
}