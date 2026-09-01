package com.theraflow.dto;

import com.theraflow.model.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        String token,
        AccountType type,
        Instant createdAt,
        Instant updatedAt
) {
}
