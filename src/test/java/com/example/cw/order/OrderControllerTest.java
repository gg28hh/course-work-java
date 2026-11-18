package com.example.cw.order;

import com.example.cw.order.model.CheckoutOrderCommand;
import com.example.cw.order.model.OrderDTO;
import com.example.cw.order.model.OrderStatus;
import com.example.cw.order.model.UpdateOrderStatusCommand;
import com.example.cw.order.services.CheckoutOrderService;
import com.example.cw.order.services.GetOrderService;
import com.example.cw.order.services.GetOrdersService;
import com.example.cw.order.services.UpdateOrderStatusService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private CheckoutOrderService checkoutOrderService;

    @Mock
    private GetOrdersService getOrdersService;

    @Mock
    private GetOrderService getOrderService;

    @Mock
    private UpdateOrderStatusService updateOrderStatusService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        OrderController controller = new OrderController(
                checkoutOrderService,
                getOrdersService,
                getOrderService,
                updateOrderStatusService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createsOrderSuccessfully() throws Exception {

        OrderDTO dto = new OrderDTO();
        dto.setId(10);
        dto.setUserId(1);
        dto.setStatus(OrderStatus.PENDING);

        when(checkoutOrderService.execute(any(CheckoutOrderCommand.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(dto));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<CheckoutOrderCommand> captor =
                ArgumentCaptor.forClass(CheckoutOrderCommand.class);

        verify(checkoutOrderService).execute(captor.capture());

        assertEquals(1, captor.getValue().getUserId());
    }

    @Test
    void getsOrdersSuccessfully() throws Exception {

        OrderDTO dto = new OrderDTO();
        dto.setId(10);
        dto.setUserId(1);

        when(getOrdersService.execute(1))
                .thenReturn(ResponseEntity.ok(List.of(dto)));

        mockMvc.perform(get("/api/orders")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(getOrdersService).execute(1);
    }

    @Test
    void getsSingleOrderSuccessfully() throws Exception {

        OrderDTO dto = new OrderDTO();
        dto.setId(10);
        dto.setUserId(1);

        when(getOrderService.execute(10))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/api/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1));

        verify(getOrderService).execute(10);
    }

    @Test
    void updatesOrderStatusSuccessfully() throws Exception {

        OrderDTO dto = new OrderDTO();
        dto.setId(10);
        dto.setStatus(OrderStatus.CONFIRMED);

        when(updateOrderStatusService.execute(
                any(UpdateOrderStatusCommand.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/api/orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONFIRMED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        ArgumentCaptor<UpdateOrderStatusCommand> captor =
                ArgumentCaptor.forClass(UpdateOrderStatusCommand.class);

        verify(updateOrderStatusService).execute(captor.capture());

        assertEquals(10, captor.getValue().getOrderId());
        assertEquals(OrderStatus.CONFIRMED, captor.getValue().getStatus());
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() throws Exception {

        when(getOrderService.execute(99))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());

        verify(getOrderService).execute(99);
    }

    @Test
    void returnsBadRequestWhenUserIdIsMissing() throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getOrdersService);
    }

    @Test
    void returnsBadRequestForInvalidOrderStatus() throws Exception {

        mockMvc.perform(patch("/api/orders/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INVALID_STATUS"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateOrderStatusService);
    }
}