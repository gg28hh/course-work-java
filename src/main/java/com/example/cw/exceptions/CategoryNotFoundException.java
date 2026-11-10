package com.example.cw.exceptions;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(ErrorMessages errorMessage) {
        super(errorMessage.getMessage());
    }
}
