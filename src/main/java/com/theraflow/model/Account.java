package com.theraflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Account {
    @Nullable
    private UUID id;
    @NonNull
    private String email;
    @NonNull
    private String passwordHash;
    @NonNull
    private AccountType type;
    @Nullable
    private Instant createdAt;
    @Nullable
    private Instant updatedAt;
}
