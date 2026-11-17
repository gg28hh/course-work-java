package com.example.cw.cart.services;

import com.example.cw.Command;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveCartItemService implements Command<RemoveCartItemCommand, CartDTO> {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public RemoveCartItemService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CartMapper cartMapper) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<CartDTO> execute(RemoveCartItemCommand command) {

        if (command == null
                || command.getUserId() == null
                || command.getUserId() <= 0
                || command.getProductId() == null
                || command.getProductId() <= 0) {

            throw new IllegalArgumentException(
                    "User ID and product ID must be positive"
            );
        }

        Cart cart = cartRepository.findByUserId(command.getUserId())
                .orElse(null);

        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {
            return ResponseEntity.notFound().build();
        }

        cart.getItems().remove(cartItem);

        cartRepository.saveAndFlush(cart);

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }
}