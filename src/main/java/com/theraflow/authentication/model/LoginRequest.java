package com.theraflow.authentication.model;

public record LoginRequest(
        String email,
        String password
) {
}
