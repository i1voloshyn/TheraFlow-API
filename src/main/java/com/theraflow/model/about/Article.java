package com.theraflow.model.about;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;

/**
 * An article written or recommended by a therapist.
 */
@NullMarked
public record Article(
        String title,
        URI url,
        @Nullable String publisher,
        @Nullable LocalDate publishedOn
) {
}
