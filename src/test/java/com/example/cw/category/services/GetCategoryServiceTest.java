package com.example.cw.category.services;

import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
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
class GetCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private GetCategoryService getCategoryService;

    @Test
    void shouldGetCategorySuccessfully() {
        // Arrange
        Integer categoryId = 1;
        Category category = new Category();
        CategoryDTO categoryDTO = mock(CategoryDTO.class);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toDTO(category))
                .thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response =
                getCategoryService.execute(categoryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(categoryDTO, response.getBody());

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toDTO(category);
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
                () -> getCategoryService.execute(categoryId)
        );

        verify(categoryRepository).findById(categoryId);
        verifyNoInteractions(categoryMapper);
    }
}
