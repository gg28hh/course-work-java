package com.example.cw.order.services;

import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private GetOrderService getOrderService;

    @Test
    void returnsOrderSuccessfully() {

        Order order = new Order(7);

        OrderDTO dto = new OrderDTO();
        dto.setId(105);
        dto.setUserId(7);

        when(orderRepository.findById(105))
                .thenReturn(Optional.of(order));

        when(orderMapper.toDTO(order))
                .thenReturn(dto);

        ResponseEntity<OrderDTO> response =
                getOrderService.execute(105);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(dto, response.getBody());
        assertEquals(105, response.getBody().getId());
        assertEquals(7, response.getBody().getUserId());

        verify(orderRepository).findById(105);
        verify(orderMapper).toDTO(order);
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() {

        when(orderRepository.findById(105))
                .thenReturn(Optional.empty());

        ResponseEntity<OrderDTO> response =
                getOrderService.execute(105);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verifyNoInteractions(orderMapper);
    }

    @Test
    void rejectsInvalidOrderId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrderService.execute(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrderService.execute(-1)
        );

        verifyNoInteractions(
                orderRepository,
                orderMapper
        );
    }

    @Test
    void rejectsNullOrderId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrderService.execute(null)
        );

        verifyNoInteractions(
                orderRepository,
                orderMapper
        );
    }
}