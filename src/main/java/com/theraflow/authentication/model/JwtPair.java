package com.theraflow.authentication.model;

public record JwtPair(
        String access,
        String refresh
) {
}
