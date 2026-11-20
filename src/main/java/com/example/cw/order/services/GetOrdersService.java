package com.example.cw.order.services;

import com.example.cw.Query;
import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.OrderDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetOrdersService
        implements Query<Integer, List<OrderDTO>> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public GetOrdersService(
            OrderRepository orderRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderDTO>> execute(Integer userId) {

        // 1. Validate user ID
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be positive"
            );
        }

        // 2. Find all orders belonging to the user
        List<OrderDTO> orders = orderRepository
                .findByUserId(userId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();

        // 3. Return the orders
        return ResponseEntity.ok(orders);
    }
}