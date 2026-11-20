package com.example.cw.cart.mapper;

import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.CartItem;
import com.example.cw.cart.model.CartItemDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartMapper {

    public CartItemDTO toItemDTO(CartItem item) {

        CartItemDTO dto = new CartItemDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());

        BigDecimal unitPrice =
                BigDecimal.valueOf(item.getProduct().getPrice());

        dto.setUnitPrice(unitPrice);
        dto.setQuantity(item.getQuantity());

        BigDecimal subtotal = unitPrice.multiply(
                BigDecimal.valueOf(item.getQuantity())
        );

        dto.setSubtotal(subtotal);

        return dto;
    }

    public CartDTO toDTO(Cart cart) {

        CartDTO dto = new CartDTO();

        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            CartItemDTO itemDTO = toItemDTO(item);

            dto.getItems().add(itemDTO);

            totalPrice = totalPrice.add(itemDTO.getSubtotal());
        }

        dto.setTotalPrice(totalPrice);

        return dto;
    }
}