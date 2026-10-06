package com.theraflow.event;

import java.util.UUID;

public record VerificationEmailRequested(
        UUID accountId, //todo: do I really need an Id here
        String email,
        String token
) {
}
