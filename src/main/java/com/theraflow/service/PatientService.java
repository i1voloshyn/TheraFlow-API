package com.theraflow.service;

import com.theraflow.dto.PatientRequest;
import com.theraflow.dto.PatientResponse;
import com.theraflow.exception.MissingTherapistProfileException;
import com.theraflow.mapper.DtoPatientMapper;
import com.theraflow.model.Therapist;
import com.theraflow.model.patient.Patient;
import com.theraflow.repository.PatientsRepository;
import com.theraflow.repository.TherapistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final TherapistRepository therapistRepository;
    private final PatientsRepository patientsRepository;
    private final DtoPatientMapper mapper;

    @Transactional
    public PatientResponse createPatient(PatientRequest request, UUID accountId) {
        Therapist therapist = therapistRepository.findByAccountId(accountId)
                .orElseThrow(MissingTherapistProfileException::new);

        Patient patient = mapper.toPatient(request);
        therapist.addPatient(patient);
        patientsRepository.saveAndFlush(patient);
        return mapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(UUID accountId, Pageable pageable) {
        return patientsRepository.findAllByTherapistAccountId(accountId, pageable)
                .map(mapper::toResponse);
    }

}
