package com.theraflow.exception;

public class JwtValidationException extends RuntimeException {
    public JwtValidationException(String message, RuntimeException e) {
        super(message, e);
    }
}
