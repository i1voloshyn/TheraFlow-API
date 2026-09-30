package com.theraflow.exception.model;

import jakarta.annotation.Nullable;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        int statusCode,
        ErrorCode errorCode,
        String title,
        String message,
        URI path,
        Instant timestamp,
        @Nullable
        List<InvalidParam> params
) {
}
