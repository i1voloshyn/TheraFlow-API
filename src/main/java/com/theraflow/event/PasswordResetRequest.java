package com.theraflow.event;

public record PasswordResetRequest(
        String email,
        String token
) {
}
