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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private GetCartService getCartService;

    @Mock
    private AddCartItemService addCartItemService;

    @Mock
    private UpdateCartItemService updateCartItemService;

    @Mock
    private RemoveCartItemService removeCartItemService;

    @Mock
    private ClearCartService clearCartService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        CartController controller = new CartController(
                getCartService,
                addCartItemService,
                updateCartItemService,
                removeCartItemService,
                clearCartService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getsCartSuccessfully() throws Exception {

        CartDTO dto = new CartDTO();
        dto.setUserId(1);

        when(getCartService.execute(1))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(getCartService).execute(1);
    }

    @Test
    void addsProductSuccessfully() throws Exception {

        when(addCartItemService.execute(any(AddCartItemCommand.class)))
                .thenReturn(ResponseEntity.ok(new CartDTO()));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "productId": 5,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AddCartItemCommand> captor =
                ArgumentCaptor.forClass(AddCartItemCommand.class);

        verify(addCartItemService).execute(captor.capture());

        assertEquals(1, captor.getValue().getUserId());
        assertEquals(5, captor.getValue().getProductId());
        assertEquals(2, captor.getValue().getQuantity());
    }

    @Test
    void updatesProductQuantitySuccessfully() throws Exception {

        when(updateCartItemService.execute(any(UpdateCartItemCommand.class)))
                .thenReturn(ResponseEntity.ok(new CartDTO()));

        mockMvc.perform(put("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "productId": 5,
                                  "quantity": 4
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateCartItemCommand> captor =
                ArgumentCaptor.forClass(UpdateCartItemCommand.class);

        verify(updateCartItemService).execute(captor.capture());

        assertEquals(1, captor.getValue().getUserId());
        assertEquals(5, captor.getValue().getProductId());
        assertEquals(4, captor.getValue().getQuantity());
    }

    @Test
    void removesProductSuccessfully() throws Exception {

        when(removeCartItemService.execute(any(RemoveCartItemCommand.class)))
                .thenReturn(ResponseEntity.ok(new CartDTO()));

        mockMvc.perform(delete("/api/cart/items")
                        .param("userId", "1")
                        .param("productId", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<RemoveCartItemCommand> captor =
                ArgumentCaptor.forClass(RemoveCartItemCommand.class);

        verify(removeCartItemService).execute(captor.capture());

        assertEquals(1, captor.getValue().getUserId());
        assertEquals(5, captor.getValue().getProductId());
    }

    @Test
    void clearsCartSuccessfully() throws Exception {

        when(clearCartService.execute(1))
                .thenReturn(ResponseEntity.ok(new CartDTO()));

        mockMvc.perform(delete("/api/cart/1/items"))
                .andExpect(status().isOk());

        verify(clearCartService).execute(1);
    }

    @Test
    void returnsBadRequestWhenProductIdIsMissing() throws Exception {

        mockMvc.perform(delete("/api/cart/items")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(removeCartItemService);
    }
}