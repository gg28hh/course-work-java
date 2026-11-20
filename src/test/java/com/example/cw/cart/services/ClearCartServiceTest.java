package com.example.cw.cart.services;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.CartItem;
import com.example.cw.product.model.Product;
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
class ClearCartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private ClearCartService clearCartService;

    @Test
    void clearsCartSuccessfully() {

        Cart cart = new Cart(1);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        CartItem item1 = new CartItem(cart, product1, 2);
        CartItem item2 = new CartItem(cart, product2, 1);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        CartDTO dto = new CartDTO();

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(cartMapper.toDTO(cart))
                .thenReturn(dto);

        ResponseEntity<CartDTO> response = clearCartService.execute(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).saveAndFlush(cart);
        verify(cartMapper).toDTO(cart);
    }

    @Test
    void clearsAlreadyEmptyCart() {

        Cart cart = new Cart(1);
        CartDTO dto = new CartDTO();

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(cartMapper.toDTO(cart))
                .thenReturn(dto);

        ResponseEntity<CartDTO> response = clearCartService.execute(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).saveAndFlush(cart);
    }

    @Test
    void returnsNotFoundWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.empty());

        ResponseEntity<CartDTO> response = clearCartService.execute(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(cartRepository, never())
                .saveAndFlush(any(Cart.class));

        verifyNoInteractions(cartMapper);
    }

    @Test
    void rejectsInvalidUserId() {

        assertThrows(IllegalArgumentException.class, () ->
                clearCartService.execute(0)
        );

        verifyNoInteractions(cartRepository, cartMapper);
    }

    @Test
    void rejectsNullUserId() {

        assertThrows(IllegalArgumentException.class, () ->
                clearCartService.execute(null)
        );

        verifyNoInteractions(cartRepository, cartMapper);
    }
}