package com.theraflow.repository;

import com.theraflow.model.patient.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientsRepository extends JpaRepository<Patient, UUID> {
}
