package com.theraflow.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class CurrentPasswordMismatchException extends BadCredentialsException {

    public CurrentPasswordMismatchException() {
        super("The old password does not match your current password");
    }
}
