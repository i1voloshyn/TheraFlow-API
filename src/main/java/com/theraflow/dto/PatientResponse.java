package com.theraflow.dto;

import com.theraflow.model.patient.Sex;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        Sex sex,
        String pesel,
        LocalDate dateOfBirth,
        Instant createdAt
) {
}
