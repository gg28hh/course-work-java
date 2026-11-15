package com.example.cw.product;

import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductService createProductService;

    @MockitoBean
    private GetProductsService getProductsService;

    @MockitoBean
    private UpdateProductService updateProductService;

    @MockitoBean
    private DeleteProductService deleteProductService;

    @MockitoBean
    private GetProductService getProductService;

    @Test
    void shouldCreateProduct() throws Exception {

        ProductDTO dto = new ProductDTO();
        dto.setId(1);
        dto.setName("Laptop");
        dto.setDescription("Powerful laptop for everyday work");
        dto.setPrice(1500.0);
        dto.setStockQuantity(10);

        when(createProductService.execute(any(Product.class)))
                .thenReturn(ResponseEntity.status(201).body(dto));

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Laptop",
                                    "description": "Powerful laptop for everyday work",
                                    "price": 1500.0,
                                    "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(createProductService).execute(any(Product.class));
    }

    @Test
    void shouldRejectInvalidProduct() throws Exception {

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "Short",
                                    "price": -10,
                                    "stockQuantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createProductService);
    }

    @Test
    void shouldGetProductById() throws Exception {

        ProductDTO dto = new ProductDTO();
        dto.setId(1);
        dto.setName("Laptop");
        dto.setDescription("Powerful laptop for everyday work");
        dto.setPrice(1500.0);
        dto.setStockQuantity(10);

        when(getProductService.execute(1))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(getProductService).execute(1);
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        when(deleteProductService.execute(1))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/product/1"))
                .andExpect(status().isNoContent());

        verify(deleteProductService).execute(1);
    }
}
