package com.theraflow.event;

import java.util.UUID;

public record VerificationEmailRequested(
        UUID accountId,
        String email,
        String token
) {
}
