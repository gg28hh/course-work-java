package com.example.cw.cart.services;

import com.example.cw.cart.CartItemRepository;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.AddCartItemCommand;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.CartItem;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCartItemServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private AddCartItemService addCartItemService;

    private Product createProduct(Integer id, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setName("Keyboard");
        product.setPrice(50.0);
        product.setStockQuantity(stock);
        return product;
    }

    @Test
    void createsCartAndAddsProduct() {

        Product product = createProduct(15, 5);
        Cart cart = new Cart(7);
        CartDTO dto = new CartDTO();

        when(productRepository.findById(15))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cart);

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.empty());

        when(cartMapper.toDTO(cart))
                .thenReturn(dto);

        ResponseEntity<CartDTO> response = addCartItemService.execute(
                new AddCartItemCommand(7, 15, 2)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());

        verify(cartItemRepository)
                .saveAndFlush(cart.getItems().get(0));
    }

    @Test
    void increasesQuantityForExistingProduct() {

        Product product = createProduct(15, 10);
        Cart cart = new Cart(7);
        CartItem existingItem = new CartItem(cart, product, 2);

        cart.getItems().add(existingItem);

        when(productRepository.findById(15))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(existingItem));

        addCartItemService.execute(
                new AddCartItemCommand(7, 15, 3)
        );

        assertEquals(5, existingItem.getQuantity());
        assertEquals(1, cart.getItems().size());

        verify(cartItemRepository).saveAndFlush(existingItem);
    }

    @Test
    void rejectsQuantityExceedingStock() {

        Product product = createProduct(15, 4);
        Cart cart = new Cart(7);
        CartItem existingItem = new CartItem(cart, product, 2);

        when(productRepository.findById(15))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(existingItem));

        assertThrows(
                IllegalArgumentException.class,
                () -> addCartItemService.execute(
                        new AddCartItemCommand(7, 15, 3)
                )
        );

        assertEquals(2, existingItem.getQuantity());

        verify(cartItemRepository, never())
                .saveAndFlush(any(CartItem.class));
    }

    @Test
    void rejectsNonExistingProduct() {

        when(productRepository.findById(15))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> addCartItemService.execute(
                        new AddCartItemCommand(7, 15, 2)
                )
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository,
                cartMapper
        );
    }

    @Test
    void rejectsInvalidQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> addCartItemService.execute(
                        new AddCartItemCommand(7, 15, 0)
                )
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository,
                productRepository,
                cartMapper
        );
    }
}