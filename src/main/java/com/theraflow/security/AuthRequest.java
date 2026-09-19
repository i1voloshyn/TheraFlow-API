package com.theraflow.security;

public record AuthRequest(
        String email,
        String password
) {
}
