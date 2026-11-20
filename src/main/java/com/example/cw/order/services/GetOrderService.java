package com.example.cw.order.services;

import com.example.cw.Query;
import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOrderService
        implements Query<Integer, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public GetOrderService(
            OrderRepository orderRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDTO> execute(Integer orderId) {

        // 1. Validate order ID
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be positive"
            );
        }

        // 2. Find the order
        Order order = orderRepository.findById(orderId)
                .orElse(null);

        // 3. Return 404 if the order does not exist
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // 4. Convert the order to DTO
        OrderDTO dto = orderMapper.toDTO(order);

        // 5. Return the order
        return ResponseEntity.ok(dto);
    }
}