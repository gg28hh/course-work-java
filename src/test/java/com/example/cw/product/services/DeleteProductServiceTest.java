package com.example.cw.product.services;

import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.model.Product;
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
class DeleteProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductService deleteProductService;

    @Test
    void shouldDeleteProductSuccessfully() {
        // Arrange
        Integer productId = 1;
        Product product = new Product();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        ResponseEntity<Void> response =
                deleteProductService.execute(productId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(productRepository).findById(productId);
        verify(productRepository).deleteById(productId);
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
                () -> deleteProductService.execute(productId)
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).deleteById(anyInt());
    }
}
