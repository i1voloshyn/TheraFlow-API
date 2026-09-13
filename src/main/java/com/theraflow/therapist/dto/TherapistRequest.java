package com.theraflow.therapist.dto;

import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record TherapistRequest(
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @NotEmpty String licenseNumber,
        @Nullable String profTitle
) {
}
