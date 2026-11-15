package com.example.cw.product.services;

import com.example.cw.category.CategoryRepository;
import com.example.cw.category.model.Category;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CreateProductService createProductService;

    @Test
    void shouldCreateProductSuccessfully() {

        // Arrange
        Product product = new Product();

        Category requestedCategory = new Category();
        requestedCategory.setId(1);

        Category existingCategory = new Category();
        existingCategory.setId(1);

        product.setCategories(Set.of(requestedCategory));

        Product savedProduct = new Product();
        ProductDTO productDTO = mock(ProductDTO.class);

        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(existingCategory));

        when(productRepository.save(product))
                .thenReturn(savedProduct);

        when(productMapper.toDTO(savedProduct))
                .thenReturn(productDTO);

        // Act
        ResponseEntity<ProductDTO> response =
                createProductService.execute(product);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(productDTO, response.getBody());

        assertTrue(product.getCategories().contains(existingCategory));

        verify(categoryRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(product);
        verify(productMapper, times(1)).toDTO(savedProduct);
    }
}