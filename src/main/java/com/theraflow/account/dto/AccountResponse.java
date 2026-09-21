package com.theraflow.account.dto;

import com.theraflow.account.model.AccountType;

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
