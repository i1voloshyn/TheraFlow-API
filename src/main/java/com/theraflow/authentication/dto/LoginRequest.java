package com.theraflow.authentication.dto;

import jakarta.validation.constraints.Email;
import org.jspecify.annotations.NonNull;

public record LoginRequest(
        @NonNull
        @Email
        String email,
        @NonNull
        String password
) {
}
