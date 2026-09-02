package com.theraflow.service;

import com.theraflow.dto.AboutRequest;
import com.theraflow.dto.AddressRequest;
import com.theraflow.dto.ProfileDetailsResponse;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.About;
import com.theraflow.model.Address;
import com.theraflow.model.Therapist;
import com.theraflow.repository.AccountRepository;
import com.theraflow.repository.TherapistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class TherapistService {
    private static final String ENTITY_NAME = "Therapist";
    private final TherapistRepository therapistRepository;
    private final AccountRepository accountRepository;
    private final DtoTherapistMapper mapper;

    public TherapistResponse createProfile(TherapistRequest request, UUID accountId) {

        Therapist therapist = therapistRepository.saveAndFlush(mapper.toTherapist(request, accountId));

        return mapper.toTherapistResponse(therapist);
    }

    @Transactional(readOnly = true)
    public ProfileDetailsResponse getProfileDetails(UUID accountId) {
        Therapist therapist = findTherapistByAccountId(accountId);
        String email = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account", accountId))
                .getEmail();

        return mapper.toProfileDetailsResponse(therapist, email);
    }

    public void updateProfile(TherapistRequest request, UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);

        actual.setFirstName(request.firstName());
        actual.setLastName(request.lastName());
        actual.setLicenseNumber(request.licenseNumber());
        actual.setProfessionalTitle(request.profTitle());
    }

    public void updateAbout(AboutRequest request, UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);

        actual.setAbout(mapper.toAbout(request));
    }

    @Transactional(readOnly = true)
    public Optional<About> getAbout(UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);

        return Optional.ofNullable(actual.getAbout());
    }

    public UUID addAddress(AddressRequest request, UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);
        UUID addressId = UUID.randomUUID();
        List<Address> addresses = mutableAddressesOf(actual);

        addresses.add(mapper.toAddress(request, addressId));
        actual.setAddress(List.copyOf(addresses));

        return addressId;
    }

    public void updateAddress(UUID addressId, AddressRequest request, UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);
        List<Address> addresses = mutableAddressesOf(actual);
        int addressIndex = findAddressIndex(addresses, addressId);

        addresses.set(addressIndex, mapper.toAddress(request, addressId));
        actual.setAddress(List.copyOf(addresses));
    }

    public void deleteAddress(UUID addressId, UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);
        List<Address> addresses = mutableAddressesOf(actual);
        int addressIndex = findAddressIndex(addresses, addressId);

        addresses.remove(addressIndex);
        actual.setAddress(List.copyOf(addresses));
    }

    @Transactional(readOnly = true)
    public List<Address> getAddresses(UUID accountId) {
        Therapist actual = findTherapistByAccountId(accountId);

        return actual.getAddress() == null
                ? List.of()
                : List.copyOf(actual.getAddress());
    }

    private Therapist findTherapistByAccountId(UUID accountId) {
        return therapistRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NAME, accountId));
    }

    private List<Address> mutableAddressesOf(Therapist therapist) {
        return therapist.getAddress() == null
                ? new ArrayList<>()
                : new ArrayList<>(therapist.getAddress());
    }

    private int findAddressIndex(List<Address> addresses, UUID addressId) {
        for (int index = 0; index < addresses.size(); index++) {
            if (addresses.get(index).id().equals(addressId)) {
                return index;
            }
        }

        throw new EntityNotFoundException("Address", addressId);
    }

}
