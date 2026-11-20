package com.example.cw.cart.services;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;

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
class GetCartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private GetCartService getCartService;

    @Test
    void returnsCartWhenExists() {

        Integer userId = 7;

        Cart cart = new Cart(userId);
        CartDTO dto = new CartDTO();
        dto.setUserId(userId);

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(cartMapper.toDTO(cart))
                .thenReturn(dto);

        ResponseEntity<CartDTO> response =
                getCartService.execute(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(cartRepository).findByUserId(userId);
        verify(cartMapper).toDTO(cart);
    }

    @Test
    void returnsNotFoundWhenCartDoesNotExist() {

        Integer userId = 7;

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        ResponseEntity<CartDTO> response =
                getCartService.execute(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verifyNoInteractions(cartMapper);
    }

    @Test
    void rejectsInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> getCartService.execute(0)
        );

        verifyNoInteractions(cartRepository, cartMapper);
    }
}