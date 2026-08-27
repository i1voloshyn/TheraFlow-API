package com.theraflow.exception.model;

public record InvalidParam(
        String name,
        String reason
) {
}
