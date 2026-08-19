package com.theraflow.exceptionHandler;

public record InvalidParam(
        String name,
        String reason
) {
}
