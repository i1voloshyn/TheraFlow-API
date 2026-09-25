package com.theraflow.authentication.model;

public record AuthTokenPair(
        String access,
        String refresh
) {
}
