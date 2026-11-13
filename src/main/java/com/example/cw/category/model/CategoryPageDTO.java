package com.example.cw.category.model;

import java.util.List;

public record CategoryPageDTO(
    List<CategoryDTO> categories,
    long totalCategories,
    int currentPage,
    int totalPages,
    int pageSize

    ){
}
