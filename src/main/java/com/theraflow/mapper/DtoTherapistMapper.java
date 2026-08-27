package com.theraflow.mapper;

import com.theraflow.model.Therapist;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DtoTherapistMapper {

    public Therapist toTherapist(TherapistRequest request, UUID accountId) {
        return Therapist.builder()
                .accountId(accountId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .licenseNumber(request.licenseNumber())
                .professionalTitle(request.profTitle())
                .bio(request.bio())
                .build();
    }

    public TherapistResponse toTherapistResponse(Therapist therapist) {
        return new TherapistResponse(
                therapist.getId(),
                therapist.getAccountId(),
                therapist.getFirstName(),
                therapist.getLastName(),
                therapist.getLicenseNumber(),
                therapist.getProfessionalTitle(),
                therapist.getBio(),
                therapist.getCreatedAt(),
                therapist.getUpdatedAt()
        );
    }
}
