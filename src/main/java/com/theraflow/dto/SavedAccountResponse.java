package com.theraflow.dto;

import com.theraflow.model.AccountType;

import java.util.UUID;

public record SavedAccountResponse(UUID id,
                                   String email,
                                   AccountType type) {
}
