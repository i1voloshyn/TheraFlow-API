package com.theraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
public class Therapist {
    @Nullable
    private UUID id;
    @NonNull
    private UUID accountId;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String licenseNumber;
    @NonNull
    private String professionalTitle;
    @NonNull
    private String bio;
    @Nullable
    private Instant createdAt;
    @Nullable
    private Instant updatedAt;
}
