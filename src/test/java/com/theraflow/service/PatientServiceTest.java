package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.PatientRequest;
import com.theraflow.exception.MissingTherapistProfileException;
import com.theraflow.mapper.DtoPatientMapperImpl;
import com.theraflow.model.Therapist;
import com.theraflow.model.account.Account;
import com.theraflow.model.account.AccountType;
import com.theraflow.model.patient.Patient;
import com.theraflow.model.patient.Sex;
import com.theraflow.repository.AccountRepository;
import com.theraflow.repository.PatientsRepository;
import com.theraflow.repository.TherapistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({TestcontainersConfiguration.class,
        PatientService.class,
        DtoPatientMapperImpl.class})
class PatientServiceTest {
    @Autowired
    private PatientService patientService;
    @Autowired
    private TherapistRepository therapistRepository;
    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private Therapist therapist;

    @Autowired
    private PatientsRepository patientsRepository;

    @DisplayName("Should create a new patient and associate it with the therapist")
    @Test
    void createPatientSuccess() {
        account();
        therapist();

        patientService.createPatient(patient(), account.getId());

        Patient savedPatient = therapist.getPatients().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No patient found for therapist"));

        assertThat(savedPatient.getId()).isNotNull();
        assertThat(savedPatient.getFirstName()).isEqualTo("Emily");
        assertThat(savedPatient.getTherapist().getId()).isEqualTo(therapist.getId());
    }

    @DisplayName("Should throw MissingTherapistProfileException when therapist profile is missing")
    @Test
    void createPatient_withoutTherapistProfile() {
        account();

        assertThatExceptionOfType(MissingTherapistProfileException.class)
                .isThrownBy(() -> patientService.createPatient(patient(), account.getId()));
    }

    @DisplayName("Should create a new patient with the same PESEL for different therapists")
    @Test
    void createPatient_withSamePesel() {
        account();
        therapist();
        Account account1 = accountRepository.save(Account.builder()
                .email("therapist1@example.com")
                .passwordHash("password-hash")
                .type(AccountType.THERAPIST)
                .build());
        Therapist therapist1 = therapistRepository.saveAndFlush(Therapist.builder()
                .account(account1)
                .firstName("Jo")
                .lastName("Miah")
                .licenseNumber("LIC457")
                .professionalTitle("Doctor")
                .build());

        patientService.createPatient(patient(), account.getId());
        patientService.createPatient(patient(), account1.getId());

        List<Patient> savedPatients = patientsRepository.findAll();

        Patient therapistPatient = therapist.getPatients().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No patient found for therapist"));
        Patient therapist1Patient = therapist1.getPatients().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No patient found for therapist1"));

        assertThat(savedPatients).hasSize(2);

        assertThat(therapist1Patient.getPesel()).isEqualTo(therapistPatient.getPesel());
        assertThat(therapist1.getId()).isNotEqualTo(therapist.getId());
    }

    private PatientRequest patient() {
        return new PatientRequest(
                "Emily",
                "Toronto",
                Sex.FEMALE,
                "12345678901",
                LocalDate.of(2020, 5, 5));
    }

    private void account() {
        account = accountRepository.save(Account.builder()
                .email("therapist@example.com")
                .passwordHash("password-hash")
                .type(AccountType.THERAPIST)
                .build());
    }

    private void therapist() {
        therapist = therapistRepository.saveAndFlush(Therapist.builder()
                .account(account)
                .firstName("Jere")
                .lastName("Miah")
                .licenseNumber("LIC456")
                .professionalTitle("Doctor")
                .build());
    }

}
