package com.example.cw.cart.services;

import com.example.cw.Command;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartService implements Command<Integer, CartDTO> {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public ClearCartService(
            CartRepository cartRepository,
            CartMapper cartMapper) {

        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<CartDTO> execute(Integer userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be positive"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        cart.getItems().clear();

        cartRepository.saveAndFlush(cart);

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }
}