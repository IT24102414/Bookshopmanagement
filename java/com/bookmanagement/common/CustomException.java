package com.bookmanagement.common;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}