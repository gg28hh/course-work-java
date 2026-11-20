package com.example.cw.cart.services;

import com.example.cw.Command;
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

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCartItemService
        implements Command<AddCartItemCommand, CartDTO> {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public AddCartItemService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<CartDTO> execute(AddCartItemCommand command) {

        if (command == null
                || command.getUserId() == null
                || command.getUserId() <= 0
                || command.getProductId() == null
                || command.getProductId() <= 0
                || command.getQuantity() == null
                || command.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "User ID, product ID and quantity must be positive"
            );
        }

        Product product = productRepository
                .findById(command.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        Cart cart = cartRepository
                .findByUserId(command.getUserId())
                .orElseGet(() ->
                        cartRepository.save(new Cart(command.getUserId()))
                );

        CartItem existingItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        long totalQuantity = command.getQuantity();

        if (existingItem != null) {
            totalQuantity += existingItem.getQuantity();
        }

        Integer availableStock = product.getStockQuantity();

        if (availableStock == null || totalQuantity > availableStock) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product"
            );
        }

        if (existingItem != null) {

            existingItem.setQuantity((int) totalQuantity);
            cartItemRepository.saveAndFlush(existingItem);

        } else {

            CartItem newItem = new CartItem(
                    cart,
                    product,
                    (int) totalQuantity
            );

            cart.getItems().add(newItem);
            cartItemRepository.saveAndFlush(newItem);
        }

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }
}