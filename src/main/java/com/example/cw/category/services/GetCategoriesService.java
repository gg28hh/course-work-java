package com.example.cw.category.services;

import com.example.cw.Query;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.model.CategoryPageDTO;
import com.example.cw.category.model.CategorySearchCriteria;
import com.example.cw.category.specification.CategorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCategoriesService
        implements Query<CategorySearchCriteria, CategoryPageDTO> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public GetCategoriesService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ResponseEntity<CategoryPageDTO> execute(CategorySearchCriteria input) {

        if (input.page() < 0 || input.size() < 1 || input.size() > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters!");
        }

        if (input.sortBy() == null ||
                !List.of("id", "name").contains(input.sortBy())) {
            throw new IllegalArgumentException("Invalid sort field!");
        }

        if (input.direction() == null ||
                (!input.direction().equalsIgnoreCase("asc") &&
                        !input.direction().equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Invalid sorting direction!");
        }

        Sort.Direction direction =
                Sort.Direction.fromString(input.direction());

        Pageable pageable = PageRequest.of(
                input.page(),
                input.size(),
                Sort.by(direction, input.sortBy())
        );

        Specification<Category> specification =
                CategorySpecification.filter(input.name());

        Page<CategoryDTO> categories = categoryRepository
                .findAll(specification, pageable)
                .map(categoryMapper::toDTO);

        CategoryPageDTO result = new CategoryPageDTO(
                categories.getContent(),
                categories.getTotalElements(),
                categories.getNumber(),
                categories.getTotalPages(),
                categories.getSize()
        );

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}