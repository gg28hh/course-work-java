package com.example.cw.order.services;

import com.example.cw.cart.CartRepository;
import com.example.cw.cart.model.Cart;
import com.example.cw.cart.model.CartItem;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.order.OrderProductRepository;
import com.example.cw.order.OrderRepository;
import com.example.cw.order.mapper.OrderMapper;
import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.Order;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.product.model.Product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutOrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CheckoutOrderService checkoutOrderService;

    private Product createProduct(
            Integer id,
            Integer stock,
            Double price
    ) {
        Product product = new Product();

        product.setId(id);
        product.setName("Keyboard");
        product.setPrice(price);
        product.setStockQuantity(stock);

        return product;
    }

    private Cart createCart(
            Integer userId,
            Product product,
            Integer quantity
    ) {
        Cart cart = new Cart(userId);

        CartItem item = new CartItem(
                cart,
                product,
                quantity
        );

        cart.getItems().add(item);

        return cart;
    }

    @Test
    void createsOrderSuccessfully() {

        Product product = createProduct(15, 10, 12.50);
        Cart cart = createCart(7, product, 2);
        OrderDTO dto = new OrderDTO();

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        when(orderProductRepository.findByIdForUpdate(15))
                .thenReturn(Optional.of(product));

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toDTO(any(Order.class)))
                .thenReturn(dto);

        ResponseEntity<OrderDTO> response =
                checkoutOrderService.execute(
                        new CheckoutOrderCommand(7)
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());

        assertEquals(8, product.getStockQuantity());
        assertTrue(cart.getItems().isEmpty());

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository)
                .saveAndFlush(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(7, savedOrder.getUserId());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(
                new BigDecimal("25.00"),
                savedOrder.getTotalPrice()
        );

        assertEquals(1, savedOrder.getItems().size());
        assertEquals(
                "Keyboard",
                savedOrder.getItems().get(0).getProductName()
        );

        assertEquals(
                new BigDecimal("12.50"),
                savedOrder.getItems().get(0).getUnitPrice()
        );

        verify(cartRepository).saveAndFlush(cart);
    }

    @Test
    void returnsNotFoundWhenCartDoesNotExist() {

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.empty());

        ResponseEntity<OrderDTO> response =
                checkoutOrderService.execute(
                        new CheckoutOrderCommand(7)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        verifyNoInteractions(
                orderRepository,
                orderProductRepository,
                orderMapper
        );
    }

    @Test
    void rejectsEmptyCart() {

        Cart cart = new Cart(7);

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutOrderService.execute(
                        new CheckoutOrderCommand(7)
                )
        );

        verifyNoInteractions(
                orderRepository,
                orderProductRepository,
                orderMapper
        );
    }

    @Test
    void rejectsInsufficientStock() {

        Product product = createProduct(15, 1, 12.50);
        Cart cart = createCart(7, product, 2);

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        when(orderProductRepository.findByIdForUpdate(15))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutOrderService.execute(
                        new CheckoutOrderCommand(7)
                )
        );

        assertEquals(1, product.getStockQuantity());
        assertEquals(1, cart.getItems().size());

        verifyNoInteractions(orderRepository, orderMapper);
        verify(cartRepository, never()).saveAndFlush(any(Cart.class));
    }

    @Test
    void rejectsNonExistingProduct() {

        Product product = createProduct(15, 10, 12.50);
        Cart cart = createCart(7, product, 2);

        when(cartRepository.findByUserId(7))
                .thenReturn(Optional.of(cart));

        when(orderProductRepository.findByIdForUpdate(15))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> checkoutOrderService.execute(
                        new CheckoutOrderCommand(7)
                )
        );

        assertEquals(1, cart.getItems().size());

        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void rejectsInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutOrderService.execute(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutOrderService.execute(
                        new CheckoutOrderCommand(null)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutOrderService.execute(
                        new CheckoutOrderCommand(0)
                )
        );

        verifyNoInteractions(
                cartRepository,
                orderRepository,
                orderProductRepository,
                orderMapper
        );
    }
}