package com.example.cw.category.services;

import com.example.cw.Command;
import com.example.cw.exceptions.CategoryAlreadyExistsException;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.cw.exceptions.ErrorMessages;

@Service
public class CreateCategoryService implements Command<Category, CategoryDTO> {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CreateCategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ResponseEntity<CategoryDTO> execute(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new CategoryAlreadyExistsException(
                    ErrorMessages.CATEGORY_ALREADY_EXISTS);
        }

        Category savedCategory = categoryRepository.save(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryMapper.toDTO(savedCategory));
    }
}
