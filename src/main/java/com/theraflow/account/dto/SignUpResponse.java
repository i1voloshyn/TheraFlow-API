package com.theraflow.account.dto;

import com.theraflow.security.model.Token;

public record SignUpResponse(
        AccountResponse account,
        Token token
) {
}
