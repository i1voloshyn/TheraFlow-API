package com.theraflow.application.refreshToken;

import java.time.Instant;

public record RefreshTokenDto(
        String token,
        Instant expiresAt
) {
}
