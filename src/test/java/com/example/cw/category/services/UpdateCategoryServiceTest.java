package com.example.cw.category.services;

import com.example.cw.exceptions.CategoryAlreadyExistsException;
import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.exceptions.ErrorMessages;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.model.UpdateCategoryCommand;
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
class UpdateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private UpdateCategoryService updateCategoryService;

    @Test
    void shouldUpdateCategorySuccessfully() {
        // Arrange
        Integer categoryId = 1;

        Category existingCategory = new Category();

        Category updatedCategory = new Category();
        updatedCategory.setName("Electronics Updated");
        updatedCategory.setDescription("Updated description");

        UpdateCategoryCommand command = mock(UpdateCategoryCommand.class);
        CategoryDTO categoryDTO = mock(CategoryDTO.class);

        when(command.getId()).thenReturn(categoryId);
        when(command.getCategory()).thenReturn(updatedCategory);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(existingCategory));

        when(categoryRepository.findByName("Electronics Updated"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(existingCategory))
                .thenReturn(existingCategory);

        when(categoryMapper.toDTO(existingCategory))
                .thenReturn(categoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response =
                updateCategoryService.execute(command);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(categoryDTO, response.getBody());

        assertEquals("Electronics Updated", existingCategory.getName());
        assertEquals("Updated description",
                existingCategory.getDescription());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findByName("Electronics Updated");
        verify(categoryRepository).save(existingCategory);
        verify(categoryMapper).toDTO(existingCategory);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        // Arrange
        Integer categoryId = 999;

        UpdateCategoryCommand command = mock(UpdateCategoryCommand.class);

        when(command.getId()).thenReturn(categoryId);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> updateCategoryService.execute(command)
        );

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).findByName(anyString());
        verify(categoryRepository, never()).save(any());
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameAlreadyExists() {
        // Arrange
        Integer categoryId = 1;

        Category existingCategory = new Category();

        Category updatedCategory = new Category();
        updatedCategory.setName("Electronics");
        updatedCategory.setDescription("Updated description");

        Category anotherCategory = new Category();
        anotherCategory.setId(2);

        UpdateCategoryCommand command = mock(UpdateCategoryCommand.class);

        when(command.getId()).thenReturn(categoryId);
        when(command.getCategory()).thenReturn(updatedCategory);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(existingCategory));

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.of(anotherCategory));

        // Act & Assert
        CategoryAlreadyExistsException exception = assertThrows(
                CategoryAlreadyExistsException.class,
                () -> updateCategoryService.execute(command)
        );

        assertEquals(
                ErrorMessages.CATEGORY_ALREADY_EXISTS.getMessage(),
                exception.getMessage()
        );

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findByName("Electronics");
        verify(categoryRepository, never()).save(any());
        verifyNoInteractions(categoryMapper);
    }
}
