package com.example.cw.order;

import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.order.model.UpdateOrderStatusCommand;
import com.example.cw.order.services.CheckoutOrderService;
import com.example.cw.order.services.GetOrderService;
import com.example.cw.order.services.GetOrdersService;
import com.example.cw.order.services.UpdateOrderStatusService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CheckoutOrderService checkoutOrderService;
    private final GetOrdersService getOrdersService;
    private final GetOrderService getOrderService;
    private final UpdateOrderStatusService updateOrderStatusService;

    public OrderController(
            CheckoutOrderService checkoutOrderService,
            GetOrdersService getOrdersService,
            GetOrderService getOrderService,
            UpdateOrderStatusService updateOrderStatusService) {

        this.checkoutOrderService = checkoutOrderService;
        this.getOrdersService = getOrdersService;
        this.getOrderService = getOrderService;
        this.updateOrderStatusService = updateOrderStatusService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(
            @RequestBody CheckoutRequest request) {

        CheckoutOrderCommand command = new CheckoutOrderCommand(
                request.userId()
        );

        return checkoutOrderService.execute(command);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(
            @RequestParam Integer userId) {

        return getOrdersService.execute(userId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(
            @PathVariable Integer orderId) {

        return getOrderService.execute(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateStatus(
            @PathVariable Integer orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        UpdateOrderStatusCommand command =
                new UpdateOrderStatusCommand(
                        orderId,
                        request.status()
                );

        return updateOrderStatusService.execute(command);
    }

    public record CheckoutRequest(
            Integer userId
    ) {
    }

    public record UpdateOrderStatusRequest(
            OrderStatus status
    ) {
    }
}