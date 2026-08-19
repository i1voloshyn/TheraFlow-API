package com.theraflow.model;

import com.theraflow.AccountType;
import jakarta.validation.constraints.Email;
import org.jspecify.annotations.NonNull;

public record AccountRequest(
        @Email
        @NonNull String email,
        @NonNull String rawPassword,
        @NonNull AccountType type
) {
}
