package com.theraflow.therapist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TherapistRepository extends JpaRepository<Therapist, UUID> {

    Optional<Therapist> findByAccountId(UUID accountId);
}
