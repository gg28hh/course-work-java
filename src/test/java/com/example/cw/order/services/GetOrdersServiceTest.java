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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrdersServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private GetOrdersService getOrdersService;

    @Test
    void returnsAllOrdersForUser() {

        Order order1 = new Order(7);
        Order order2 = new Order(7);

        OrderDTO dto1 = new OrderDTO();
        OrderDTO dto2 = new OrderDTO();

        dto1.setId(101);
        dto2.setId(102);

        when(orderRepository.findByUserId(7))
                .thenReturn(List.of(order1, order2));

        when(orderMapper.toDTO(order1))
                .thenReturn(dto1);

        when(orderMapper.toDTO(order2))
                .thenReturn(dto2);

        ResponseEntity<List<OrderDTO>> response =
                getOrdersService.execute(7);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        assertSame(dto1, response.getBody().get(0));
        assertSame(dto2, response.getBody().get(1));

        verify(orderRepository).findByUserId(7);
        verify(orderMapper).toDTO(order1);
        verify(orderMapper).toDTO(order2);
    }

    @Test
    void returnsEmptyListWhenUserHasNoOrders() {

        when(orderRepository.findByUserId(7))
                .thenReturn(List.of());

        ResponseEntity<List<OrderDTO>> response =
                getOrdersService.execute(7);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verifyNoInteractions(orderMapper);
    }

    @Test
    void rejectsInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrdersService.execute(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrdersService.execute(-1)
        );

        verifyNoInteractions(
                orderRepository,
                orderMapper
        );
    }

    @Test
    void rejectsNullUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> getOrdersService.execute(null)
        );

        verifyNoInteractions(
                orderRepository,
                orderMapper
        );
    }
}