package com.theraflow.patient.dto;

import com.theraflow.patient.model.Sex;

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
