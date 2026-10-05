package com.theraflow.authentication.dto;

public record TokenRotateRequest(
        String refreshToken
) {
}
