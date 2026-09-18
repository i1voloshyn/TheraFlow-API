package com.theraflow.patient;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/patients")
public class PatientController {
    private final PatientService patientService;

//    @PostMapping
//    public ResponseEntity<PatientResponse> createPatient(
//            @Valid @RequestBody PatientRequest request,
//            @AuthenticationPrincipal AccountPrincipal accountPrincipal
//    ) {
//        var patient = patientService.createPatient(request, accountPrincipal.getAccountId());
//
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("{id")
//                .buildAndExpand(patient.id())
//                .toUri();
//
//        return ResponseEntity.created(location).body(patient);
//    }
//
//    @GetMapping
//    public ResponseEntity<PagedModel<PatientResponse>> getAll(
//            @AuthenticationPrincipal AccountPrincipal accountPrincipal,
//            @PageableDefault(
//                    size = 20,
//                    sort = {"createdAt", "id"},
//                    direction = Sort.Direction.DESC
//            ) Pageable pageable
//    ) {
//        var patients = patientService.getAll(accountPrincipal.getAccountId(), pageable);
//
//        return ResponseEntity.ok(new PagedModel<>(patients));
//    }

}
