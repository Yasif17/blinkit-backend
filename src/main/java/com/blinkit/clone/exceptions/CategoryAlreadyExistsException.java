package com.blinkit.clone.exceptions;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(){}

    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
