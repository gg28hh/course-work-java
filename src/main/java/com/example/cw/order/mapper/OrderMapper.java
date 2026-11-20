package com.example.cw.order.mapper;

import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderItem;
import com.example.cw.order.model.OrderItemDTO;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderItemDTO toItemDTO(OrderItem item) {

        OrderItemDTO dto = new OrderItemDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProductName());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }

    public OrderDTO toDTO(Order order) {

        OrderDTO dto = new OrderDTO();

        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());

        for (OrderItem item : order.getItems()) {

            OrderItemDTO itemDTO = toItemDTO(item);

            dto.getItems().add(itemDTO);
        }

        return dto;
    }
}