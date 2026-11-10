package com.example.cw.exceptions;

public class CategoryAlreadyExistsException extends RuntimeException
{
    public CategoryAlreadyExistsException(ErrorMessages errorMessage) {
        super(errorMessage.getMessage());
    }
}
