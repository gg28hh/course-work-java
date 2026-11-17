package com.example.cw.cart.services;

import com.example.cw.Query;
import com.example.cw.cart.CartRepository;
import com.example.cw.cart.mapper.CartMapper;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCartService implements Query<Integer, CartDTO> {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public GetCartService(
            CartRepository cartRepository,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional(readOnly = true)
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

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }
}