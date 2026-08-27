package com.theraflow.service;

import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.Therapist;
import com.theraflow.repository.TherapistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class TherapistService {
    private static final String ENTITY_NAME = "Therapist";
    private final TherapistRepository therapistRepository;
    private final DtoTherapistMapper mapper;

    public TherapistResponse createTherapistProfile(TherapistRequest request, UUID accountId) {

        Therapist therapist = therapistRepository.saveAndFlush(mapper.toTherapist(request, accountId));

        return mapper.toTherapistResponse(therapist);
    }

    public void updateTherapistProfile(TherapistRequest request, UUID id) {
        Therapist actual = therapistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NAME, id));

        actual.setFirstName(request.firstName());
        actual.setLastName(request.lastName());
        actual.setProfessionalTitle(request.profTitle());
        actual.setBio(request.bio());
    }

}
