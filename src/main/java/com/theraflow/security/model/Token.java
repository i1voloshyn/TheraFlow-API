package com.theraflow.security.model;

public record Token(
        String access,
        String refresh
) {
}
