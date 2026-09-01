package com.theraflow.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@NullMarked
public record TherapistResponse(
        UUID id,
        UUID accountId,
        String firstName,
        String lastName,
        String licenseNumber,
        @Nullable String profTitle,
        Instant createdAt,
        Instant updatedAt
) {
}
