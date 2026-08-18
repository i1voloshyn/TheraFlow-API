package com.theraflow.model;

import com.theraflow.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        AccountType type,
        Instant createdAt,
        Instant updatedAt
) {
}
