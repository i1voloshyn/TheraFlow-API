package com.theraflow.account.dto;

public record AuthenticationResponse(
        AccountResponse account,
        String accessToken
) {
}
