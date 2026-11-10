
package com.example.cw.category;

import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.model.UpdateCategoryCommand;
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
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        return getCategoriesService.execute(null);
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
