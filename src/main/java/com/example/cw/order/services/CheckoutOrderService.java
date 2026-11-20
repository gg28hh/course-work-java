package com.example.cw.order.services;

import com.example.cw.Command;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.order.OrderProductRepository;
import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderItem;
import com.example.cw.product.model.Product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CheckoutOrderService
        implements Command<CheckoutOrderCommand, OrderDTO> {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderMapper orderMapper;

    public CheckoutOrderService(
            CartRepository cartRepository,
            OrderRepository orderRepository,
            OrderProductRepository orderProductRepository,
            OrderMapper orderMapper
    ) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<OrderDTO> execute(CheckoutOrderCommand command) {

        // 1. Validate user ID
        if (command == null
                || command.getUserId() == null
                || command.getUserId() <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be positive"
            );
        }

        Integer userId = command.getUserId();

        // 2. Find the user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Check whether the cart is empty
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot checkout an empty cart"
            );
        }

        // 4. Sort products by ID to reduce deadlock risk
        List<CartItem> cartItems =
                new ArrayList<>(cart.getItems());

        cartItems.sort(
                Comparator.comparingInt(
                        item -> item.getProduct().getId()
                )
        );

        Order order = new Order(userId);

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 5. Process every product
        for (CartItem cartItem : cartItems) {

            Integer productId = cartItem.getProduct().getId();

            // Lock product while processing the order
            Product product = orderProductRepository
                    .findByIdForUpdate(productId)
                    .orElseThrow(ProductNotFoundException::new);

            Integer quantity = cartItem.getQuantity();
            Integer availableStock = product.getStockQuantity();

            // 6. Validate available stock
            if (quantity == null
                    || quantity <= 0
                    || availableStock == null
                    || availableStock < quantity) {

                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + productId
                );
            }

            // 7. Validate and capture the current price
            Double price = product.getPrice();

            if (price == null || !Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException(
                        "Invalid price for product: " + productId
                );
            }

            BigDecimal unitPrice = BigDecimal.valueOf(price)
                    .setScale(2, RoundingMode.HALF_UP);

            // 8. Decrease the available stock
            product.setStockQuantity(availableStock - quantity);

            // 9. Create an order item
            OrderItem orderItem = new OrderItem(
                    product,
                    quantity,
                    unitPrice
            );

            order.addItem(orderItem);

            totalPrice = totalPrice.add(
                    orderItem.getSubtotal()
            );
        }

        // 10. Save the total price
        order.setTotalPrice(totalPrice);

        // 11. Save the order and its items
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 12. Clear the shopping cart
        cart.getItems().clear();

        cartRepository.saveAndFlush(cart);

        // 13. Return the completed order
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderMapper.toDTO(savedOrder));
    }
}