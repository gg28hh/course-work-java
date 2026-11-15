package com.example.cw.category;

import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.services.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCategoryService createCategoryService;

    @MockitoBean
    private GetCategoriesService getCategoriesService;

    @MockitoBean
    private UpdateCategoryService updateCategoryService;

    @MockitoBean
    private DeleteCategoryService deleteCategoryService;

    @MockitoBean
    private GetCategoryService getCategoryService;


    @Test
    void createCategory_shouldReturnCreatedCategory() throws Exception {

        CategoryDTO dto = new CategoryDTO();
        dto.setId(1);
        dto.setName("Electronics");
        dto.setDescription("Electronic products");

        when(createCategoryService.execute(any(Category.class)))
                .thenReturn(ResponseEntity.status(201).body(dto));

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Electronics",
                                    "description": "Electronic products"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description")
                        .value("Electronic products"));

        verify(createCategoryService).execute(any(Category.class));
    }


    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {

        CategoryDTO dto = new CategoryDTO();
        dto.setId(1);
        dto.setName("Electronics");
        dto.setDescription("Electronic products");

        when(getCategoryService.execute(1))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description")
                        .value("Electronic products"));

        verify(getCategoryService).execute(1);
    }


    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {

        when(deleteCategoryService.execute(1))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/category/1"))
                .andExpect(status().isNoContent());

        verify(deleteCategoryService).execute(1);
    }
}
