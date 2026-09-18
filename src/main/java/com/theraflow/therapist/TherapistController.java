package com.theraflow.therapist;

import com.theraflow.therapist.dto.ProfileDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/therapist")
public class TherapistController {

    private final TherapistService therapistService;

//    @PostMapping
//    public ResponseEntity<TherapistResponse> createTherapistProfile(
//            @Valid @RequestBody TherapistRequest request
//          //  @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        TherapistResponse therapist = therapistService.createProfile(request, principal.getAccountId());
//
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(therapist.id())
//                .toUri();
//
//        return ResponseEntity.created(location).body(therapist);
//    }
//
//    @GetMapping("/me")
//    public ResponseEntity<ProfileDetailsResponse> getProfileDetails(
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        return ResponseEntity.ok(therapistService.getProfileDetails(principal.getAccountId()));
//    }
//
//    @PutMapping
//    public ResponseEntity<Void> updateProfile(
//            @Valid @RequestBody TherapistRequest request,
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        therapistService.updateProfile(request, principal.getAccountId());
//
//        return ResponseEntity.noContent().build();
//    }
//
//    @PutMapping("/about")
//    public ResponseEntity<Void> updateTherapistAbout(
//            @Valid @RequestBody AboutRequest request,
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        therapistService.updateAbout(request, principal.getAccountId());
//
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/about")
//    public ResponseEntity<About> getAbout(
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        return ResponseEntity.of(therapistService.getAbout(principal.getAccountId()));
//    }
//
//    @PostMapping("/addresses")
//    public ResponseEntity<Void> addAddress(
//            @Valid @RequestBody AddressRequest request,
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        UUID addressId = therapistService.addAddress(request, principal.getAccountId());
//
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{addressId}")
//                .buildAndExpand(addressId)
//                .toUri();
//
//        return ResponseEntity.created(location).build();
//    }
//
//    @PutMapping("/addresses/{addressId}")
//    public ResponseEntity<Void> updateAddress(
//            @PathVariable UUID addressId,
//            @Valid @RequestBody AddressRequest request,
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        therapistService.updateAddress(addressId, request, principal.getAccountId());
//
//        return ResponseEntity.noContent().build();
//    }
//
//    @DeleteMapping("/addresses/{addressId}")
//    public ResponseEntity<Void> deleteAddress(
//            @PathVariable UUID addressId,
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        therapistService.deleteAddress(addressId, principal.getAccountId());
//
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/addresses")
//    public ResponseEntity<List<Address>> getAddresses(
//            @AuthenticationPrincipal AccountPrincipal principal
//    ) {
//        return ResponseEntity.ok(therapistService.getAddresses(principal.getAccountId()));
//    }


    @GetMapping("/me")
    public ResponseEntity<ProfileDetailsResponse> getProfileDetails(
    ) {

        return ResponseEntity.ok(therapistService.getProfileDetails(UUID.randomUUID()));
    }

}
