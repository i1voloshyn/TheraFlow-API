package com.theraflow.authentication.dto;

public record SignUpResponse(
        AccountResponse account,
        JwtPair tokens
) {
}
