package com.theraflow.account.dto;

import com.theraflow.authentication.model.JwtPair;

public record SignUpResponse(
        AccountResponse account,
        JwtPair tokens
) {
}
