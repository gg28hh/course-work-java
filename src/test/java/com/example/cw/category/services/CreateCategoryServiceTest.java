package com.example.cw.category.services;

import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.exceptions.CategoryAlreadyExistsException;
import com.example.cw.exceptions.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CreateCategoryService createCategoryService;

    @Test
    void shouldCreateCategorySuccessfully() {

        // Arrange
        Category category = new Category();
        category.setName("Electronics");

        Category savedCategory = new Category();
        savedCategory.setName("Electronics");

        CategoryDTO categoryDTO = mock(CategoryDTO.class);

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(false);

        when(categoryRepository.save(category))
                .thenReturn(savedCategory);

        when(categoryMapper.toDTO(savedCategory))
                .thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response =
                createCategoryService.execute(category);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertSame(categoryDTO, response.getBody());

        verify(categoryRepository, times(1))
                .existsByName("Electronics");

        verify(categoryRepository, times(1))
                .save(category);

        verify(categoryMapper, times(1))
                .toDTO(savedCategory);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        // Arrange
        Category category = new Category();
        category.setName("Electronics");

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(true);

        // Act & Assert
        CategoryAlreadyExistsException exception = assertThrows(
                CategoryAlreadyExistsException.class,
                () -> createCategoryService.execute(category)
        );

        assertEquals(
                ErrorMessages.CATEGORY_ALREADY_EXISTS.getMessage(),
                exception.getMessage()
        );

        verify(categoryRepository, times(1))
                .existsByName("Electronics");

        verify(categoryRepository, never()).save(any(Category.class));

        verifyNoInteractions(categoryMapper);
    }
}
