package com.theraflow.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@NullMarked
public record ProfileDetailsResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String licenseNumber,
        @Nullable String professionalTitle,
        Instant createdAt,
        Instant updatedAt
) {
}
