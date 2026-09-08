package com.theraflow.model;

import com.theraflow.model.about.Article;
import com.theraflow.model.about.Education;
import com.theraflow.model.about.Experience;
import com.theraflow.model.about.Language;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Content displayed in the "About" section of a therapist's profile.
 *
 * <p>The value is persisted as JSON, so it is modelled as an immutable value
 * rather than as a JPA entity. Missing JSON collections are normalised to
 * empty lists to support both {@code null} values and an empty JSON object.</p>
 */
@NullMarked
public record About(
        @Nullable String bio,
        List<Language> languages,
        List<Education> education,
        List<Experience> experience,
        List<Article> articles
) {

    public About {
        languages = copyOrEmpty(languages);
        education = copyOrEmpty(education);
        experience = copyOrEmpty(experience);
        articles = copyOrEmpty(articles);
    }

    private static <T> List<T> copyOrEmpty(@Nullable List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
