package com.theraflow.patient.dto;

import com.theraflow.patient.model.Sex;

import java.time.LocalDate;

public record PatientRequest(
        String firstName,
        String lastName,
        Sex sex,
        String pesel,
        LocalDate dateOfBirth
) {
}
