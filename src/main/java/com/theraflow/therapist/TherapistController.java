package com.theraflow.therapist;

import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.about.About;
import com.theraflow.therapist.about.Address;
import com.theraflow.therapist.dto.AboutRequest;
import com.theraflow.therapist.dto.AddressRequest;
import com.theraflow.therapist.dto.ProfileDetailsResponse;
import com.theraflow.therapist.dto.TherapistRequest;
import com.theraflow.therapist.dto.TherapistResponse;
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
@RequestMapping("/api/v1/therapist")
public class TherapistController {

    private final TherapistService therapistService;

    @PostMapping
    public ResponseEntity<TherapistResponse> createTherapistProfile(
            @Valid @RequestBody TherapistRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        TherapistResponse therapist = therapistService.createProfile(request, user.getAccountId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(therapist.id())
                .toUri();

        return ResponseEntity.created(location).body(therapist);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDetailsResponse> getProfileDetails(
            @AuthenticationPrincipal TheraflowUser user
    ) {
        return ResponseEntity.ok(therapistService.getProfileDetails(user.getAccountId()));
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody TherapistRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        therapistService.updateProfile(request, user.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/about")
    public ResponseEntity<Void> updateTherapistAbout(
            @Valid @RequestBody AboutRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        therapistService.updateAbout(request, user.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/about")
    public ResponseEntity<About> getAbout(
            @AuthenticationPrincipal TheraflowUser user
    ) {
        return ResponseEntity.of(therapistService.getAbout(user.getAccountId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<Void> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        UUID addressId = therapistService.addAddress(request, user.getAccountId());

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
            @AuthenticationPrincipal TheraflowUser user
    ) {
        therapistService.updateAddress(addressId, request, user.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        therapistService.deleteAddress(addressId, user.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(
            @AuthenticationPrincipal TheraflowUser user
    ) {
        return ResponseEntity.ok(therapistService.getAddresses(user.getAccountId()));
    }


}
