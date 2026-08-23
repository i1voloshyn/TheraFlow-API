package com.theraflow.service;

import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.Therapist;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.repository.TherapistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TherapistService {
    private final TherapistRepository therapistRepository;
    private final DtoTherapistMapper mapper;

    @Transactional
    public TherapistResponse createTherapistProfile(TherapistRequest request) {

        Therapist therapist = therapistRepository.save(mapper.toTherapist(request));

        return mapper.toTherapistResponse(therapist);
    }
}
