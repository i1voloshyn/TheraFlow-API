package com.theraflow.exception.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // ========== Authentication & Authorization ==========
    AUTHENTICATION_FAILED(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed",
            "You may have entered the wrong email address or password or your account might be locked."),

    PASSWORD_INCORRECT(
            HttpStatus.UNAUTHORIZED,
            "Password incorrect",
            "The provided password does not match your current password"),

    PASSWORD_NOT_MATCH(
            HttpStatus.BAD_REQUEST,
            "Password not match",
            "The new password and confirm password do not match"
    ),

    PASSWORD_WEAK(
            HttpStatus.BAD_REQUEST,
            "Password weak",
            "Password does not meet security requirements. Use at least 10 characters with uppercase, lowercase, numbers, and symbols"),

    // ========== Access Errors ==========
    PASSWORD_RESET_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "Invalid password reset token.",
            "The password reset link is invalid or has expired. Please request a new password reset link."),

    EMAIL_VERIFICATION_LINK_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Email verification link expired",
            "The email verification link has expired. Please request a new verification email"),

    EMAIL_NOT_VERIFIED(
            HttpStatus.UNAUTHORIZED,
            "Email not verified",
            "Verify your account before you sign in or request a verification email."),

    TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "Token invalid",
            "The provided tokens is invalid or malformed. Please log in again"),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            "The provided tokens has expired. Please log in again or use a refresh tokens to obtain a new one"),

    REFRESH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            "The provided refresh token has expired. Please log in again"
    ),

    REFRESH_TOKEN_REVOKED(
            HttpStatus.UNAUTHORIZED,
            "Refresh tokens revoked",
            "The refresh tokens has been revoked. Please log in again"),

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
