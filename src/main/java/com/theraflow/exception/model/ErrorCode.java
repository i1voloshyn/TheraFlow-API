package com.theraflow.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements TheraflowErrorCode {
    // ========== Authentication & Authorization ==========
    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed",
            "Invalid email or password"),

    PASSWORD_INCORRECT(
            HttpStatus.BAD_REQUEST,
            "Password incorrect",
            "The provided password does not match your current password"),

    PASSWORD_WEAK(
            HttpStatus.BAD_REQUEST,
            "Password weak",
            "Password does not meet security requirements. Use at least 8 characters with uppercase, lowercase, numbers, and symbols"),

    // ========== Token Errors ==========
    TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "Token invalid",
            "The provided token is invalid or malformed. Please log in again"),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            "The token has expired. Please log in again or use a refresh token to obtain a new one"),

    REFRESH_TOKEN_REVOKED(
            HttpStatus.UNAUTHORIZED,
            "Refresh token revoked",
            "The refresh token has been revoked. Please log in again"),

    // ========== Resource Errors ==========
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            "The requested resource does not exist"),

    RESOURCE_CONFLICT(
            HttpStatus.CONFLICT,
            "Resource conflict",
            "A resource with the provided data already exists"),

    // ========== Validation Errors ==========
    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "One or more fields are invalid. See 'errors' for details");

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
