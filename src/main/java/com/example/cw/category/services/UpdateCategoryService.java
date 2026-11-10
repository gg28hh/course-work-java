package com.example.cw.category.services;

import com.example.cw.Command;
import com.example.cw.exceptions.CategoryAlreadyExistsException;
import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.exceptions.ErrorMessages;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.model.UpdateCategoryCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateCategoryService
        implements Command<UpdateCategoryCommand, CategoryDTO> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public UpdateCategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ResponseEntity<CategoryDTO> execute(
            UpdateCategoryCommand command) {

        Optional<Category> optionalCategory =
                categoryRepository.findById(command.getId());

        if (optionalCategory.isEmpty()) {
            throw new CategoryNotFoundException(
                    ErrorMessages.CATEGORY_NOT_FOUND);
        }

        Category existingCategory = optionalCategory.get();
        Category updatedCategory = command.getCategory();

        Optional<Category> categoryWithSameName =
                categoryRepository.findByName(updatedCategory.getName());

        if (categoryWithSameName.isPresent()
                && !categoryWithSameName.get().getId()
                .equals(command.getId())) {

            throw new CategoryAlreadyExistsException(
                    ErrorMessages.CATEGORY_ALREADY_EXISTS);
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(
                updatedCategory.getDescription());

        Category savedCategory =
                categoryRepository.save(existingCategory);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryMapper.toDTO(savedCategory));
    }
}
