package com.example.cw.cart;

import com.example.cw.cart.model.AddCartItemCommand;
import com.example.cw.cart.model.CartDTO;
import com.example.cw.cart.model.RemoveCartItemCommand;
import com.example.cw.cart.model.UpdateCartItemCommand;
import com.example.cw.cart.services.AddCartItemService;
import com.example.cw.cart.services.ClearCartService;
import com.example.cw.cart.services.GetCartService;
import com.example.cw.cart.services.RemoveCartItemService;
import com.example.cw.cart.services.UpdateCartItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final GetCartService getCartService;
    private final AddCartItemService addCartItemService;
    private final UpdateCartItemService updateCartItemService;
    private final RemoveCartItemService removeCartItemService;
    private final ClearCartService clearCartService;

    public CartController(
            GetCartService getCartService,
            AddCartItemService addCartItemService,
            UpdateCartItemService updateCartItemService,
            RemoveCartItemService removeCartItemService,
            ClearCartService clearCartService) {

        this.getCartService = getCartService;
        this.addCartItemService = addCartItemService;
        this.updateCartItemService = updateCartItemService;
        this.removeCartItemService = removeCartItemService;
        this.clearCartService = clearCartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCart(
            @PathVariable Integer userId) {

        return getCartService.execute(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @RequestBody CartItemRequest request) {

        AddCartItemCommand command = new AddCartItemCommand(
                request.userId(),
                request.productId(),
                request.quantity()
        );

        return addCartItemService.execute(command);
    }

    @PutMapping("/items")
    public ResponseEntity<CartDTO> updateItem(
            @RequestBody CartItemRequest request) {

        UpdateCartItemCommand command = new UpdateCartItemCommand(
                request.userId(),
                request.productId(),
                request.quantity()
        );

        return updateCartItemService.execute(command);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartDTO> removeItem(
            @RequestParam Integer userId,
            @RequestParam Integer productId) {

        RemoveCartItemCommand command = new RemoveCartItemCommand(
                userId,
                productId
        );

        return removeCartItemService.execute(command);
    }

    @DeleteMapping("/{userId}/items")
    public ResponseEntity<CartDTO> clearCart(
            @PathVariable Integer userId) {

        return clearCartService.execute(userId);
    }

    public record CartItemRequest(
            Integer userId,
            Integer productId,
            Integer quantity
    ) {
    }
}