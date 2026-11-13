package com.example.cw.category.model;

public record CategorySearchCriteria(
    String name,
    int page,
    int size,
    String sortBy,
    String direction

) {

}

