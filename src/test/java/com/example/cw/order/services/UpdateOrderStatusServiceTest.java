package com.example.cw.order.services;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private UpdateOrderStatusService updateOrderStatusService;

    @Test
    void confirmsPendingOrder() {

        Order order = findOrder(1, OrderStatus.PENDING);
        OrderDTO dto = mockSuccessfulSave(order);

        ResponseEntity<OrderDTO> response = updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.CONFIRMED)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        verify(orderRepository).saveAndFlush(order);
        verifyNoInteractions(orderProductRepository);
    }

    @Test
    void shipsConfirmedOrder() {

        Order order = findOrder(1, OrderStatus.CONFIRMED);
        mockSuccessfulSave(order);

        updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.SHIPPED)
        );

        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        verify(orderRepository).saveAndFlush(order);
        verifyNoInteractions(orderProductRepository);
    }

    @Test
    void deliversShippedOrder() {

        Order order = findOrder(1, OrderStatus.SHIPPED);
        mockSuccessfulSave(order);

        updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.DELIVERED)
        );

        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        verify(orderRepository).saveAndFlush(order);
        verifyNoInteractions(orderProductRepository);
    }

    @Test
    void cancelsPendingOrderAndRestoresStock() {

        Order order = findOrder(1, OrderStatus.PENDING);

        Product product = createProduct(10, 5);

        order.addItem(
                new OrderItem(product, 3, BigDecimal.TEN)
        );

        when(orderProductRepository.findByIdForUpdate(10))
                .thenReturn(Optional.of(product));

        mockSuccessfulSave(order);

        updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.CANCELLED)
        );

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(8, product.getStockQuantity());

        verify(orderProductRepository).findByIdForUpdate(10);
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void cancelsConfirmedOrderAndRestoresStock() {

        Order order = findOrder(1, OrderStatus.CONFIRMED);

        Product product = createProduct(10, 7);

        order.addItem(
                new OrderItem(product, 2, BigDecimal.TEN)
        );

        when(orderProductRepository.findByIdForUpdate(10))
                .thenReturn(Optional.of(product));

        mockSuccessfulSave(order);

        updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.CANCELLED)
        );

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(9, product.getStockQuantity());

        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void locksProductsInIdOrderDuringCancellation() {

        Order order = findOrder(1, OrderStatus.PENDING);

        Product product10 = createProduct(10, 5);
        Product product3 = createProduct(3, 8);

        order.addItem(
                new OrderItem(product10, 2, BigDecimal.TEN)
        );

        order.addItem(
                new OrderItem(product3, 1, BigDecimal.TEN)
        );

        when(orderProductRepository.findByIdForUpdate(3))
                .thenReturn(Optional.of(product3));

        when(orderProductRepository.findByIdForUpdate(10))
                .thenReturn(Optional.of(product10));

        mockSuccessfulSave(order);

        updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.CANCELLED)
        );

        assertEquals(7, product10.getStockQuantity());
        assertEquals(9, product3.getStockQuantity());

        var inOrder = inOrder(orderProductRepository);

        inOrder.verify(orderProductRepository).findByIdForUpdate(3);
        inOrder.verify(orderProductRepository).findByIdForUpdate(10);
    }

    @Test
    void doesNotRestoreStockAgainWhenAlreadyCancelled() {

        Order order = findOrder(1, OrderStatus.CANCELLED);

        Product product = createProduct(10, 5);

        order.addItem(
                new OrderItem(product, 3, BigDecimal.TEN)
        );

        OrderDTO dto = new OrderDTO();

        when(orderMapper.toDTO(order)).thenReturn(dto);

        ResponseEntity<OrderDTO> response = updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(1, OrderStatus.CANCELLED)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertEquals(5, product.getStockQuantity());

        verifyNoInteractions(orderProductRepository);
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsCancellationAfterShipping() {

        Order order = findOrder(1, OrderStatus.SHIPPED);

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                1, OrderStatus.CANCELLED
                        )
                )
        );

        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        verify(orderRepository, never()).saveAndFlush(any());
        verifyNoInteractions(orderProductRepository);
    }

    @Test
    void rejectsStatusChangeAfterDelivery() {

        Order order = findOrder(1, OrderStatus.DELIVERED);

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                1, OrderStatus.CANCELLED
                        )
                )
        );

        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() {

        when(orderRepository.findByIdForUpdate(99))
                .thenReturn(Optional.empty());

        ResponseEntity<OrderDTO> response = updateOrderStatusService.execute(
                new UpdateOrderStatusCommand(99, OrderStatus.CONFIRMED)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(orderRepository, never()).saveAndFlush(any());
        verifyNoInteractions(orderProductRepository, orderMapper);
    }

    @Test
    void rejectsNullCommand() {

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(null)
        );

        verifyNoInteractions(
                orderRepository,
                orderProductRepository,
                orderMapper
        );
    }

    @Test
    void rejectsInvalidOrderId() {

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                0, OrderStatus.CONFIRMED
                        )
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsNullStatus() {

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(1, null)
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsCancellationWhenProductDoesNotExist() {

        Order order = findOrder(1, OrderStatus.PENDING);

        Product product = createProduct(10, 5);

        order.addItem(
                new OrderItem(product, 2, BigDecimal.TEN)
        );

        when(orderProductRepository.findByIdForUpdate(10))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                1, OrderStatus.CANCELLED
                        )
                )
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsStockOverflowDuringCancellation() {

        Order order = findOrder(1, OrderStatus.PENDING);

        Product product = createProduct(10, Integer.MAX_VALUE);

        order.addItem(
                new OrderItem(product, 2, BigDecimal.TEN)
        );

        when(orderProductRepository.findByIdForUpdate(10))
                .thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () ->
                updateOrderStatusService.execute(
                        new UpdateOrderStatusCommand(
                                1, OrderStatus.CANCELLED
                        )
                )
        );

        assertEquals(Integer.MAX_VALUE, product.getStockQuantity());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(orderRepository, never()).saveAndFlush(any());
    }

    private Order findOrder(Integer id, OrderStatus status) {

        Order order = new Order(1);
        order.setStatus(status);

        when(orderRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(order));

        return order;
    }

    private OrderDTO mockSuccessfulSave(Order order) {

        OrderDTO dto = new OrderDTO();

        when(orderRepository.saveAndFlush(order))
                .thenReturn(order);

        when(orderMapper.toDTO(order))
                .thenReturn(dto);

        return dto;
    }

    private Product createProduct(Integer id, Integer stock) {

        Product product = new Product();

        product.setId(id);
        product.setName("Product " + id);
        product.setStockQuantity(stock);

        return product;
    }
}