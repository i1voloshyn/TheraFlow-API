package com.theraflow.controller;

import com.theraflow.dto.TherapistResponse;
import com.theraflow.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/therapist-profile")
public class TherapistProfileController {

    private final TherapistService therapistService;

    @PostMapping()
    public ResponseEntity<TherapistResponse> createTherapistProfile(
    ) {

    }

}
