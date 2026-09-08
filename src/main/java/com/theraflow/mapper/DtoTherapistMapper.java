package com.theraflow.mapper;

import com.theraflow.dto.AboutRequest;
import com.theraflow.dto.AddressRequest;
import com.theraflow.dto.ProfileDetailsResponse;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.model.About;
import com.theraflow.model.Account;
import com.theraflow.model.about.Address;
import com.theraflow.model.Therapist;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DtoTherapistMapper {

    public Therapist toTherapist(TherapistRequest request, Account account) {
        return Therapist.builder()
                .account(account)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .licenseNumber(request.licenseNumber())
                .professionalTitle(request.profTitle())
                .build();
    }

    public TherapistResponse toTherapistResponse(Therapist therapist) {
        return new TherapistResponse(
                therapist.getId(),
                therapist.getAccount().getId(),
                therapist.getFirstName(),
                therapist.getLastName(),
                therapist.getLicenseNumber(),
                therapist.getProfessionalTitle(),
                therapist.getCreatedAt(),
                therapist.getUpdatedAt()
        );
    }

    public ProfileDetailsResponse toProfileDetailsResponse(Therapist therapist, String email) {
        return new ProfileDetailsResponse(
                therapist.getId(),
                email,
                therapist.getFirstName(),
                therapist.getLastName(),
                therapist.getLicenseNumber(),
                therapist.getProfessionalTitle(),
                therapist.getCreatedAt(),
                therapist.getUpdatedAt()
        );
    }

    public About toAbout(AboutRequest request) {
        return new About(
                request.bio(),
                request.languages(),
                request.education(),
                request.experience(),
                request.articles()
        );
    }

    public Address toAddress(AddressRequest request, UUID id) {
        return new Address(
                id,
                request.street(),
                request.buildingNumber(),
                request.apartmentNumber(),
                request.city(),
                request.region(),
                request.postalCode(),
                request.countryCode(),
                request.phoneNumber()
        );
    }
}
