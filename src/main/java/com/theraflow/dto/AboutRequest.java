package com.theraflow.dto;

import com.theraflow.model.Article;
import com.theraflow.model.Education;
import com.theraflow.model.Experience;
import com.theraflow.model.Language;
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
