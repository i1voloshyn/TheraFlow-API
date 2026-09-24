package com.theraflow.authentication.model;

public record Token(
        String access,
        String refresh
) {
}
