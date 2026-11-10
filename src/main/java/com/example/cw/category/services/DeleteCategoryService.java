package com.example.cw.category.services;

import com.example.cw.Command;
import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.exceptions.ErrorMessages;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeleteCategoryService implements Command<Integer, Void> {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryService(
            CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ResponseEntity<Void> execute(Integer id) {

        Optional<Category> optionalCategory =
                categoryRepository.findById(id);

        if (optionalCategory.isEmpty()) {
            throw new CategoryNotFoundException(
                    ErrorMessages.CATEGORY_NOT_FOUND);
        }

        categoryRepository.delete(optionalCategory.get());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
