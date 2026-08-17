package com.theraflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @Nullable
    private String passwordHash;
    @NonNull
    private AccountType type;
    @Nullable
    private Instant createdAt;
    @Nullable
    private Instant updatedAt;
}
