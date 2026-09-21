package com.theraflow.patient;

import com.theraflow.patient.dto.PatientRequest;
import com.theraflow.patient.dto.PatientResponse;
import com.theraflow.security.model.TheraflowUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/patients")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        var patient = patientService.createPatient(request, user.getAccountId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id")
                .buildAndExpand(patient.id())
                .toUri();

        return ResponseEntity.created(location).body(patient);
    }

    @GetMapping
    public ResponseEntity<PagedModel<PatientResponse>> getAll(
            @AuthenticationPrincipal TheraflowUser user,
            @PageableDefault(
                    size = 20,
                    sort = {"createdAt", "id"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        var patients = patientService.getAll(user.getAccountId(), pageable);

        return ResponseEntity.ok(new PagedModel<>(patients));
    }

}
