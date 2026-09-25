package com.theraflow.account.dto;

import com.theraflow.authentication.model.AuthTokenPair;

public record SignUpResponse(
        AccountResponse account,
        AuthTokenPair token
) {
}
