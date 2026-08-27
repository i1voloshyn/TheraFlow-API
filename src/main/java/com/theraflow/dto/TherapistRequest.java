package com.theraflow.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public record TherapistRequest(
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @NotEmpty String licenseNumber,
        @Nullable String profTitle,
        @Nullable String bio
) {
}
