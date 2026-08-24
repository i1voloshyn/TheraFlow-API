package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

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
    @Autowired
    private TherapistService service;

    private UUID accountId;

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

        TherapistResponse actual = service.createTherapistProfile(request);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.createdAt()).isNotNull();
        assertThat(actual.updatedAt()).isNotNull();
    }

    @Test
    void createTherapistProfile_shouldPreserveNullOptionalFields() {
        TherapistRequest request = requestWith(null, null);

        TherapistResponse actual = service.createTherapistProfile(request);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.profTitle()).isNull();
        assertThat(actual.bio()).isNull();
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
