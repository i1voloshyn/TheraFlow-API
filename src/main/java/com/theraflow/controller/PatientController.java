package com.theraflow.controller;

import com.theraflow.dto.PatientRequest;
import com.theraflow.dto.PatientResponse;
import com.theraflow.security.model.AccountPrincipal;
import com.theraflow.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/patients")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @RequestBody PatientRequest request,
            @AuthenticationPrincipal AccountPrincipal accountPrincipal
    ) {
        var patient = patientService.createPatient(request, accountPrincipal.getAccountId());
        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

}
