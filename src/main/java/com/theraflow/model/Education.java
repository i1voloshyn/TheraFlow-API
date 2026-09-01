package com.theraflow.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * An item in a therapist's education history.
 */
@NullMarked
public record Education(
        String institution,
        String qualification,
        @Nullable String fieldOfStudy,
        @Nullable LocalDate startedOn,
        @Nullable LocalDate completedOn
) {
}
