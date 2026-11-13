package com.example.cw.category;

import com.example.cw.category.model.*;
import com.example.cw.category.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
public class CategoryController {

    private final CreateCategoryService createCategoryService;
    private final GetCategoriesService getCategoriesService;
    private final UpdateCategoryService updateCategoryService;
    private final DeleteCategoryService deleteCategoryService;
    private final GetCategoryService getCategoryService;

    public CategoryController(CreateCategoryService createCategoryService,
                              GetCategoriesService getCategoriesService,
                              UpdateCategoryService updateCategoryService,
                              DeleteCategoryService deleteCategoryService,
                              GetCategoryService getCategoryService) {
        this.createCategoryService = createCategoryService;
        this.getCategoriesService = getCategoriesService;
        this.updateCategoryService = updateCategoryService;
        this.deleteCategoryService = deleteCategoryService;
        this.getCategoryService = getCategoryService;
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody Category category) {

        return createCategoryService.execute(category);
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody Category category) {

        return updateCategoryService.execute(
                new UpdateCategoryCommand(id, category)
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<CategoryPageDTO> getCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        CategorySearchCriteria criteria = new CategorySearchCriteria(
                name,
                page,
                size,
                sortBy,
                direction
        );

        return getCategoriesService.execute(criteria);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Integer id) {

        return getCategoryService.execute(id);
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id) {

        return deleteCategoryService.execute(id);
    }
}
