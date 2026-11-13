package com.example.cw.product.model;

public record ProductSearchCriteria(
        String name,
        Integer categoryId,
        Double minPrice,
        Double maxPrice,
        int page,
        int size,
        String sortBy,
        String direction
) {
}
