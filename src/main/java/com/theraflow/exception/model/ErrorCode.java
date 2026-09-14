package com.theraflow.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    PASSWORD_MISMATCH(
            HttpStatus.UNAUTHORIZED,
            "Incorrect old password",
            "The old password does not match your current password"),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed", "Invalid email or password"),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "Weak password", "Password does not meet the security requirements"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Requested entity was not found"),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "Resource conflict",
            "The data you provided conflicts with an existing record."),

    INVALID_INPUT(HttpStatus.BAD_REQUEST,
            "Invalid input",
            "Validation failed for one or more fields. Check 'errors' for details.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String title, String message) {
        this.httpStatus = httpStatus;
        this.title = title;
        this.message = message;
    }

    ErrorCode(HttpStatus httpStatus, String title) {
        this.httpStatus = httpStatus;
        this.title = title;
        this.message = null;
    }

}
