package com.example.cw.exceptions;

public enum ErrorMessages {
    PRODUCT_NOT_FOUND("Product Not Found!"),
    NAME_REQUIRED("Name is required!"),
    DESCRIPTION_LENGTH("Description must be longer!"),
    PRICE_CANNOT_BE_NEGATIVE("Price cannot be negative!"),
    CATEGORY_NOT_FOUND("Category Not Found!"),
    CATEGORY_ALREADY_EXISTS("Category already exists!");
    //CATEGORY_NAME_REQUIRED("Category name is required!");

    private final String message;

    ErrorMessages(String message){
        this.message=message;
    }

    public String getMessage(){
        return message;
    }
}
