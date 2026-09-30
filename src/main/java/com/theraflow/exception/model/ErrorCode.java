package com.theraflow.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode{
    // ========== Authentication & Authorization ==========
    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed",
            "Invalid email or password"),

    PASSWORD_INCORRECT(
            HttpStatus.UNAUTHORIZED,
            "Password incorrect",
            "The provided password does not match your current password"),

    PASSWORD_WEAK(
            HttpStatus.BAD_REQUEST,
            "Password weak",
            "Password does not meet security requirements. Use at least 10 characters with uppercase, lowercase, numbers, and symbols"),

    // ========== Access Errors ==========
    EMAIL_VERIFICATION_LINK_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Email verification link expired",
            "The email verification link has expired. Please request a new verification email"),

    TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "Token invalid",
            "The provided token is invalid or malformed. Please log in again"),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            "The provided token has expired. Please log in again or use a refresh token to obtain a new one"),

    REFRESH_TOKEN_REVOKED(
            HttpStatus.UNAUTHORIZED,
            "Refresh token revoked",
            "The refresh token has been revoked. Please log in again"),

    // ========== Not Found Errors ==========
    ACCOUNT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Account not found",
            "No account exists for the given identifier"),

    THERAPIST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Therapist not found",
            "No therapist profile exists for the given identifier"),

    PATIENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Patient not found",
            "No patient exists for the given identifier"),

    ADDRESS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Address not found",
            "No address exists for the given identifier"),

    // ========== Conflict Errors ==========
    PASSWORD_SAME_AS_OLD(
            HttpStatus.BAD_REQUEST,
            "New password same as old",
            " New password must be different from the current password"),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Email already exists",
            "An account with the provided email already exists"),

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

}
