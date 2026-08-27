package com.theraflow.controller;

import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.model.Therapist;
import com.theraflow.security.model.AccountPrincipal;
import com.theraflow.service.TherapistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.naming.AuthenticationException;
import java.net.URI;
import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/therapist-profile")
public class TherapistProfileController {

    private final TherapistService therapistService;

    @PostMapping
    public ResponseEntity<TherapistResponse> createTherapistProfile(
            @Valid @RequestBody TherapistRequest request,
            @AuthenticationPrincipal AccountPrincipal principal
    ) {
        TherapistResponse therapist = therapistService.createTherapistProfile(request, principal.getAccountId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(therapist.id())
                .toUri();

        return ResponseEntity.created(location).body(therapist);
    }

}
