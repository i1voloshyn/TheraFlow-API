package com.theraflow.account.dto;

import com.theraflow.authentication.dto.JwtPair;

public record SignUpResponse(
        AccountResponse account,
        JwtPair tokens
) {
}
