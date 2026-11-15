package com.example.cw.product.services;

import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.UpdateProductCommand;
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
class UpdateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private UpdateProductService updateProductService;

    @Test
    void shouldUpdateProductSuccessfully() {
        // Arrange
        Integer productId = 1;

        Product existingProduct = new Product();

        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated product description");
        updatedProduct.setPrice(25.50);
        updatedProduct.setStockQuantity(20);

        UpdateProductCommand command = mock(UpdateProductCommand.class);
        ProductDTO productDTO = mock(ProductDTO.class);

        when(command.getId()).thenReturn(productId);
        when(command.getProduct()).thenReturn(updatedProduct);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        when(productMapper.toDTO(existingProduct))
                .thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response =
                updateProductService.execute(command);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(productDTO, response.getBody());

        assertEquals("Updated Product", existingProduct.getName());
        assertEquals("Updated product description",
                existingProduct.getDescription());
        assertEquals(25.50, existingProduct.getPrice());
        assertEquals(20, existingProduct.getStockQuantity());

        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
        verify(productMapper).toDTO(existingProduct);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        // Arrange
        Integer productId = 999;

        UpdateProductCommand command = mock(UpdateProductCommand.class);

        when(command.getId()).thenReturn(productId);

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> updateProductService.execute(command)
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(productMapper);
    }
}
