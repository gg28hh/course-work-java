package com.example.cw.product.services;

import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private GetProductService getProductService;

    @Test
    void shouldGetProductSuccessfully() {
        // Arrange
        Integer productId = 1;
        Product product = new Product();
        ProductDTO productDTO = mock(ProductDTO.class);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toDTO(product))
                .thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response =
                getProductService.execute(productId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(productDTO, response.getBody());

        verify(productRepository).findById(productId);
        verify(productMapper).toDTO(product);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        // Arrange
        Integer productId = 999;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> getProductService.execute(productId)
        );

        verify(productRepository).findById(productId);
        verifyNoInteractions(productMapper);
    }
}
