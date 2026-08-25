package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.model.Therapist;
import com.theraflow.repository.AccountRepository;
import com.theraflow.repository.TherapistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({TestcontainersConfiguration.class,
        TherapistService.class,
        DtoTherapistMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TherapistServiceTest {

    @Autowired
    private AccountRepository accountRepository;
    private UUID accountId;
    @Autowired
    private TherapistService therapistService;
    @Autowired
    private TherapistRepository therapistRepository;

    @BeforeEach
    void setUp() {
        Account account = Account.builder()
                .email("therapist@example.com")
                .passwordHash("password-hash")
                .type(AccountType.THERAPIST)
                .build();

        accountId = accountRepository.saveAndFlush(account).getId();
    }

    @Test
    void createTherapistProfile_shouldPersistMappedTherapistAndReturnCompleteResponse() {
        TherapistRequest request = requestWith("Doctor", "Some bio");

        TherapistResponse actual = therapistService.createTherapistProfile(request);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.createdAt()).isNotNull();
        assertThat(actual.updatedAt()).isNotNull();
    }

    @Test
    void createTherapistProfile_shouldPreserveNullOptionalFields() {
        TherapistRequest request = requestWith(null, null);

        TherapistResponse actual = therapistService.createTherapistProfile(request);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.profTitle()).isNull();
        assertThat(actual.bio()).isNull();
    }

    @Test
    void updateTherapistProfile_shouldSuccessfullyUpdateProfile() {
        TherapistRequest request = requestWith("Mgr", "Some bio");
        TherapistRequest updateRequest = requestWith("Doctor", "Some bio");

        TherapistResponse actual = therapistService.createTherapistProfile(request);

        therapistService.updateTherapistProfile(updateRequest, actual.id());

        Optional<Therapist> updated = therapistRepository.findById(actual.id());

        assertThat(updated).isNotEmpty();
        assertThat(updated.get().getId()).isEqualTo(actual.id());
        assertThat(updated.get().getProfessionalTitle()).isEqualTo("Doctor");

    }


    private TherapistRequest requestWith(String professionalTitle, String bio) {
        return new TherapistRequest(
                accountId,
                "Jere",
                "Miah",
                "LIC456",
                professionalTitle,
                bio
        );
    }
}
