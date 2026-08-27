package com.theraflow.exception.model;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String title,
        URI path,
        int status,
        ErrorCode errorCode,
        Instant timestamp,
        String message,
        @Nullable List<InvalidParam> params
) {
    public ErrorResponse(
            String title,
            URI path,
            int status,
            ErrorCode errorCode,
            String message,
            List<InvalidParam> params) {
        this(title, path, status,errorCode, Instant.now(), message, params);
    }
}
