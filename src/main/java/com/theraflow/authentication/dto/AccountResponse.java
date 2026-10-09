package com.theraflow.authentication.dto;

import com.theraflow.authentication.model.AccountType;

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
