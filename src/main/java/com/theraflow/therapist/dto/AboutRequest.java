package com.theraflow.therapist.dto;

import com.theraflow.therapist.about.Article;
import com.theraflow.therapist.about.Education;
import com.theraflow.therapist.about.Experience;
import com.theraflow.therapist.about.Language;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public record AboutRequest(
        @Nullable String bio,
        List<Language> languages,
        List<Education> education,
        List<Experience> experience,
        List<Article> articles
) {
}
