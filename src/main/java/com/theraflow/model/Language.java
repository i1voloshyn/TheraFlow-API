package com.theraflow.model;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A language in which a therapist can provide therapy.
 */
@NullMarked
public record Language(
        String name,
        @Nullable String proficiency
) {
}
