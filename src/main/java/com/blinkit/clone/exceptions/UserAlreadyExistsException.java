package com.blinkit.clone.exceptions;

public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(){}

    public UserAlreadyExistsException(String message){
        super(message);
    }
}
