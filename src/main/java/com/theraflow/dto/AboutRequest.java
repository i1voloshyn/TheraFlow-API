package com.theraflow.dto;

import com.theraflow.model.about.Article;
import com.theraflow.model.about.Education;
import com.theraflow.model.about.Experience;
import com.theraflow.model.about.Language;
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
