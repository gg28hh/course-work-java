package com.example.cw.cart.services;

import com.example.cw.cart.CartItemRepository;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.CartItem;
import com.example.cw.cart.model.UpdateCartItemCommand;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
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
class UpdateCartItemServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private UpdateCartItemService updateCartItemService;

    @Test
    void updatesQuantitySuccessfully() {

        Cart cart = new Cart(1);
        Product product = mock(Product.class);
        CartItem item = new CartItem(cart, product, 2);
        cart.getItems().add(item);

        CartDTO dto = new CartDTO();

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(item));

        when(product.getStockQuantity()).thenReturn(10);

        when(cartMapper.toDTO(cart)).thenReturn(dto);

        ResponseEntity<CartDTO> response = updateCartItemService.execute(
                new UpdateCartItemCommand(1, 5, 4)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertEquals(Integer.valueOf(4), item.getQuantity());

        verify(cartItemRepository).saveAndFlush(item);
    }

    @Test
    void returnsNotFoundWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.empty());

        ResponseEntity<CartDTO> response = updateCartItemService.execute(
                new UpdateCartItemCommand(1, 5, 3)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verifyNoInteractions(productRepository, cartItemRepository, cartMapper);
    }

    @Test
    void returnsNotFoundWhenProductIsNotInCart() {

        Cart cart = new Cart(1);
        Product product = mock(Product.class);

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.empty());

        ResponseEntity<CartDTO> response = updateCartItemService.execute(
                new UpdateCartItemCommand(1, 5, 3)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(cartItemRepository, never())
                .saveAndFlush(any(CartItem.class));
    }

    @Test
    void rejectsQuantityExceedingStock() {

        Cart cart = new Cart(1);
        Product product = mock(Product.class);
        CartItem item = new CartItem(cart, product, 2);

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(item));

        when(product.getStockQuantity()).thenReturn(3);

        assertThrows(IllegalArgumentException.class, () ->
                updateCartItemService.execute(
                        new UpdateCartItemCommand(1, 5, 5)
                )
        );

        assertEquals(Integer.valueOf(2), item.getQuantity());

        verify(cartItemRepository, never())
                .saveAndFlush(any(CartItem.class));
    }

    @Test
    void rejectsInvalidQuantity() {

        assertThrows(IllegalArgumentException.class, () ->
                updateCartItemService.execute(
                        new UpdateCartItemCommand(1, 5, 0)
                )
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository,
                productRepository,
                cartMapper
        );
    }

    @Test
    void rejectsNonExistingProduct() {

        Cart cart = new Cart(1);

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                updateCartItemService.execute(
                        new UpdateCartItemCommand(1, 5, 3)
                )
        );

        verifyNoInteractions(cartItemRepository, cartMapper);
    }
}