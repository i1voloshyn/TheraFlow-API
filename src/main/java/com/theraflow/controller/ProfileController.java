package com.theraflow.controller;

import com.theraflow.dto.AboutRequest;
import com.theraflow.dto.AddressRequest;
import com.theraflow.dto.ProfileDetailsResponse;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.model.About;
import com.theraflow.model.about.Address;
import com.theraflow.security.model.AccountPrincipal;
import com.theraflow.service.TherapistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final TherapistService therapistService;

    @PostMapping
    public ResponseEntity<TherapistResponse> createTherapistProfile(
            @Valid @RequestBody TherapistRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        TherapistResponse therapist = therapistService.createProfile(request, principal.getAccountId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(therapist.id())
                .toUri();

        return ResponseEntity.created(location).body(therapist);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDetailsResponse> getProfileDetails(
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        return ResponseEntity.ok(therapistService.getProfileDetails(principal.getAccountId()));
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody TherapistRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        therapistService.updateProfile(request, principal.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/about")
    public ResponseEntity<Void> updateTherapistAbout(
            @Valid @RequestBody AboutRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        therapistService.updateAbout(request, principal.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/about")
    public ResponseEntity<About> getAbout(
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        return ResponseEntity.of(therapistService.getAbout(principal.getAccountId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<Void> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        UUID addressId = therapistService.addAddress(request, principal.getAccountId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{addressId}")
                .buildAndExpand(addressId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        therapistService.updateAddress(addressId, request, principal.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        therapistService.deleteAddress(addressId, principal.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        return ResponseEntity.ok(therapistService.getAddresses(principal.getAccountId()));
    }

}
