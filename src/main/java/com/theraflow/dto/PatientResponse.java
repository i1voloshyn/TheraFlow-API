package com.theraflow.dto;

import com.theraflow.model.patient.Sex;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        String firstName,
        String lastName,
        Sex sex,
        String pesel,
        LocalDate dateOfBirth,
        Instant createdAt
) {
}
