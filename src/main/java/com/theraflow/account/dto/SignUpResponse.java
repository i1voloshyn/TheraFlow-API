package com.theraflow.account.dto;

import com.theraflow.authentication.model.Token;

public record SignUpResponse(
        AccountResponse account,
        Token token
) {
}
