package com.blinkit.clone.exceptions;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(){}

    public ProductNotFoundException(String message) {
        super(message);
    }
}
