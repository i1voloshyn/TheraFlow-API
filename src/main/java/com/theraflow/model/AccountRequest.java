package com.theraflow.model;

import com.theraflow.AccountType;
import org.jspecify.annotations.NonNull;

public record AccountRequest(
        @NonNull String email,
        @NonNull String rawPassword,
        @NonNull AccountType type
) {
}
