package com.example.cw.cart.services;

import com.example.cw.cart.CartItemRepository;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.CartItem;
import com.example.cw.cart.model.RemoveCartItemCommand;
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
class RemoveCartItemServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private RemoveCartItemService removeCartItemService;

    @Test
    void removesProductSuccessfully() {

        Cart cart = new Cart(1);
        Product product = mock(Product.class);
        Product anotherProduct = mock(Product.class);

        CartItem item = new CartItem(cart, product, 2);
        CartItem anotherItem = new CartItem(cart, anotherProduct, 1);

        cart.getItems().add(item);
        cart.getItems().add(anotherItem);

        CartDTO dto = new CartDTO();

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(item));

        when(cartMapper.toDTO(cart)).thenReturn(dto);

        ResponseEntity<CartDTO> response = removeCartItemService.execute(
                new RemoveCartItemCommand(1, 5)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        assertFalse(cart.getItems().contains(item));
        assertTrue(cart.getItems().contains(anotherItem));
        assertEquals(1, cart.getItems().size());

        verify(cartRepository).saveAndFlush(cart);
        verify(cartMapper).toDTO(cart);
    }

    @Test
    void returnsNotFoundWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.empty());

        ResponseEntity<CartDTO> response = removeCartItemService.execute(
                new RemoveCartItemCommand(1, 5)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verifyNoInteractions(
                productRepository,
                cartItemRepository,
                cartMapper
        );
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

        ResponseEntity<CartDTO> response = removeCartItemService.execute(
                new RemoveCartItemCommand(1, 5)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(cartRepository, never())
                .saveAndFlush(any(Cart.class));
    }

    @Test
    void rejectsNonExistingProduct() {

        Cart cart = new Cart(1);

        when(cartRepository.findByUserId(1))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(5))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                removeCartItemService.execute(
                        new RemoveCartItemCommand(1, 5)
                )
        );

        verifyNoInteractions(cartItemRepository, cartMapper);
    }

    @Test
    void rejectsInvalidUserId() {

        assertThrows(IllegalArgumentException.class, () ->
                removeCartItemService.execute(
                        new RemoveCartItemCommand(0, 5)
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
    void rejectsNullCommand() {

        assertThrows(IllegalArgumentException.class, () ->
                removeCartItemService.execute(null)
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository,
                productRepository,
                cartMapper
        );
    }
}