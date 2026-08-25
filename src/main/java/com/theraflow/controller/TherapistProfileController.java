package com.theraflow.controller;

import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.service.TherapistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/therapist-profile")
public class TherapistProfileController {

    private final TherapistService therapistService;

    @PostMapping
    public ResponseEntity<TherapistResponse> createTherapistProfile(
            @Valid @RequestBody TherapistRequest request
    ) {
        TherapistResponse therapist = therapistService.createTherapistProfile(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(therapist.id())
                .toUri();

        return ResponseEntity.created(location).body(therapist);
    }

}
