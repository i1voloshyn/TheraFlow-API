package com.theraflow.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * An item in a therapist's professional history.
 */
@NullMarked
public record Experience(
        String position,
        String organization,
        @Nullable LocalDate startedOn,
        @Nullable LocalDate endedOn,
        @Nullable String description
) {
}
