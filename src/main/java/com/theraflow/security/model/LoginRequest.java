package com.theraflow.security.model;

public record LoginRequest(
        String email,
        String password
) {
}
