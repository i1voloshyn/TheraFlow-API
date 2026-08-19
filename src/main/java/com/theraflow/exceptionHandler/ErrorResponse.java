package com.theraflow.exceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String title,
        URI path,
        int status,
        Instant timestamp,
        String message,
        List<InvalidParam> params
) {
    public ErrorResponse(
            String title,
            URI path,
            int statusCode,
            String message,
            List<InvalidParam> params) {
        this(title, path, statusCode, Instant.now(), message, params);
    }
}
