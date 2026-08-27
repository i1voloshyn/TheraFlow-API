package com.theraflow.repository;

import com.theraflow.model.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TherapistRepository extends JpaRepository<Therapist, UUID> {

    Optional<Therapist> findTherapistByAccountId(UUID accountId);
}
