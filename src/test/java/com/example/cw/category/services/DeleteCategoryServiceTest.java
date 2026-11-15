package com.example.cw.category.services;

import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.model.Category;
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
class DeleteCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DeleteCategoryService deleteCategoryService;

    @Test
    void shouldDeleteCategorySuccessfully() {
        // Arrange
        Integer categoryId = 1;
        Category category = new Category();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        // Act
        ResponseEntity<Void> response =
                deleteCategoryService.execute(categoryId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        // Arrange
        Integer categoryId = 999;

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> deleteCategoryService.execute(categoryId)
        );

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
