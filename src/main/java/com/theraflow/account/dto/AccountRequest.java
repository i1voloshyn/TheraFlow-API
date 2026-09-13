package com.theraflow.account.dto;

import com.theraflow.account.model.AccountType;
import jakarta.validation.constraints.Email;
import org.jspecify.annotations.NonNull;

public record AccountRequest(
        @Email
        @NonNull String email,
        @NonNull String rawPassword,
        @NonNull AccountType type
) {
}
