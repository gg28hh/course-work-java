
package com.example.cw.category.services;

import com.example.cw.category.CategoryRepository;
import com.example.cw.category.mapper.CategoryMapper;
import com.example.cw.category.model.Category;
import com.example.cw.category.model.CategoryDTO;
import com.example.cw.category.model.CategoryPageDTO;
import com.example.cw.category.model.CategorySearchCriteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCategoriesServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private GetCategoriesService getCategoriesService;

    @Test
    void shouldReturnCategoriesSuccessfully() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        "Electronics", 0, 2, "name", "asc");

        Category category = mock(Category.class);

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(1);
        categoryDTO.setName("Electronics");
        categoryDTO.setDescription("Electronic products");

        Pageable pageable = PageRequest.of(
                0, 2, Sort.by(Sort.Direction.ASC, "name"));

        Page<Category> categoryPage = new PageImpl<>(
                List.of(category), pageable, 5);

        when(categoryRepository.searchCategories(
                eq("Electronics"), any(Pageable.class)))
                .thenReturn(categoryPage);

        when(categoryMapper.toDTO(category))
                .thenReturn(categoryDTO);

        ResponseEntity<CategoryPageDTO> response =
                getCategoriesService.execute(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());

        CategoryPageDTO result = response.getBody();

        assertEquals(1, result.categories().size());
        assertEquals(5, result.totalCategories());
        assertEquals(0, result.currentPage());
        assertEquals(3, result.totalPages());
        assertEquals(2, result.pageSize());

        assertEquals("Electronics",
                result.categories().get(0).getName());

        verify(categoryRepository)
                .searchCategories(eq("Electronics"), any(Pageable.class));

        verify(categoryMapper).toDTO(category);
    }

    @Test
    void shouldReturnEmptyPageWhenNoCategoriesFound() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        "Unknown", 0, 10, "id", "asc");

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.ASC, "id"));

        Page<Category> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(categoryRepository.searchCategories(
                eq("Unknown"), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<CategoryPageDTO> response =
                getCategoriesService.execute(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().categories().isEmpty());
        assertEquals(0, response.getBody().totalCategories());
        assertEquals(0, response.getBody().totalPages());

        verify(categoryRepository)
                .searchCategories(eq("Unknown"), any(Pageable.class));

        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldPassCorrectPaginationAndSorting() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 2, 5, "name", "desc");

        Pageable pageable = PageRequest.of(
                2, 5, Sort.by(Sort.Direction.DESC, "name"));

        when(categoryRepository.searchCategories(
                isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        getCategoriesService.execute(criteria);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(categoryRepository).searchCategories(
                isNull(), captor.capture());

        Pageable captured = captor.getValue();

        assertEquals(2, captured.getPageNumber());
        assertEquals(5, captured.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                captured.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void shouldThrowExceptionWhenPageIsNegative() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, -1, 10, "id", "asc");

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSizeIsInvalid() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 0, 0, "id", "asc");

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSizeExceedsLimit() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 0, 101, "id", "asc");

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSortFieldIsInvalid() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 0, 10, "description", "asc");

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSortDirectionIsInvalid() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 0, 10, "name", "random");

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenSortDirectionIsNull() {

        CategorySearchCriteria criteria =
                new CategorySearchCriteria(
                        null, 0, 10, "name", null);

        assertThrows(
                IllegalArgumentException.class,
                () -> getCategoriesService.execute(criteria));

        verifyNoInteractions(categoryRepository);
    }
}
