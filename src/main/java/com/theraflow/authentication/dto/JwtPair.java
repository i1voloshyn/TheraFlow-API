package com.theraflow.authentication.dto;

public record JwtPair(
        String access,
        String refresh
) {
}
