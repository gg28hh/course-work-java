package com.example.cw.category.services;

import com.example.cw.Query;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.CategoryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCategoriesService
        implements Query<Void, List<CategoryDTO>> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public GetCategoriesService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ResponseEntity<List<CategoryDTO>> execute(Void input) {

        List<CategoryDTO> categories = categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categories);
    }
}
