package com.theraflow.patient.dto;

import com.theraflow.patient.model.Sex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.pl.PESEL;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        Sex sex,
        @NotBlank
        @PESEL
        String pesel,
        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate dateOfBirth
) {
}
