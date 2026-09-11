package com.theraflow.dto;

import com.theraflow.model.patient.Sex;

import java.time.LocalDate;

public record PatientRequest(
        String firstName,
        String lastName,
        Sex sex,
        String pesel,
        LocalDate dateOfBirth
) {
}
