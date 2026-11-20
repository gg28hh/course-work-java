package com.example.cw.order.services;

import com.example.cw.Command;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.order.OrderProductRepository;
import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderItem;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.order.model.UpdateOrderStatusCommand;
import com.example.cw.product.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UpdateOrderStatusService
        implements Command<UpdateOrderStatusCommand, OrderDTO> {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderMapper orderMapper;

    public UpdateOrderStatusService(
            OrderRepository orderRepository,
            OrderProductRepository orderProductRepository,
            OrderMapper orderMapper) {

        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<OrderDTO> execute(
            UpdateOrderStatusCommand command) {

        if (command == null
                || command.getOrderId() == null
                || command.getOrderId() <= 0
                || command.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Valid order ID and status are required"
            );
        }

        Order order = orderRepository
                .findByIdForUpdate(command.getOrderId())
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = command.getStatus();

        if (currentStatus == newStatus) {
            return ResponseEntity.ok(orderMapper.toDTO(order));
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.saveAndFlush(order);

        return ResponseEntity.ok(orderMapper.toDTO(savedOrder));
    }

    private boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        return switch (currentStatus) {

            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };
    }

    private void restoreStock(Order order) {

        List<OrderItem> items = new ArrayList<>(order.getItems());

        items.sort(
                Comparator.comparingInt(
                        item -> item.getProduct().getId()
                )
        );

        for (OrderItem item : items) {

            Integer productId = item.getProduct().getId();

            Product product = orderProductRepository
                    .findByIdForUpdate(productId)
                    .orElseThrow(ProductNotFoundException::new);

            Integer currentStock = product.getStockQuantity();

            if (currentStock == null || currentStock < 0) {
                throw new IllegalArgumentException(
                        "Invalid stock quantity for product " + productId
                );
            }

            long restoredStock =
                    (long) currentStock + item.getQuantity();

            if (restoredStock > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Stock quantity overflow for product " + productId
                );
            }

            product.setStockQuantity((int) restoredStock);
        }
    }
}