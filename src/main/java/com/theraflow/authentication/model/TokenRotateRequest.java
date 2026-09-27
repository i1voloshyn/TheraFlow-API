package com.theraflow.authentication.model;

public record TokenRotateRequest(
        String refreshToken
) {
}
