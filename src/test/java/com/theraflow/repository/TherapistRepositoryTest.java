package com.theraflow.repository;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.model.Therapist;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TherapistRepositoryTest {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TherapistRepository repository;
    @Autowired
    EntityManager entityManager;

    @Test
    void createProfile_shouldCreate_andReturnSuccessfully_createdTherapistProfile() {
        Account account = Account.builder()
                .email("test@email")
                .passwordHash("valid_passpowrd_hash")
                .type(AccountType.THERAPIST)
                .build();

        Account saved = accountRepository.save(account);

        Therapist therapistToSave = Therapist.builder()
                .accountId(saved.getId())
                .firstName("Jeremiah")
                .lastName("Nevada")
                .licenseNumber("RTF 5456")
                .professionalTitle("Doctor")
                .build();

        Therapist actual = repository.saveAndFlush(therapistToSave);
        entityManager.clear();

        assertThat(repository.count()).isOne();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getFirstName()).isEqualTo("Jeremiah");
        assertThat(actual.getLastName()).isEqualTo("Nevada");
        assertThat(actual.getLicenseNumber()).isEqualTo("RTF 5456");
        assertThat(actual.getProfessionalTitle()).isEqualTo("Doctor");
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getUpdatedAt()).isNotNull();
    }

    @Test
    void createProfile_shouldThrownException_forInvalidAccountId() {
        UUID randomId = UUID.randomUUID();

        Therapist therapistToSave = Therapist.builder()
                .accountId(randomId)
                .firstName("Jeremiah")
                .lastName("Nevada")
                .licenseNumber("RTF 5456")
                .professionalTitle("Doctor")
                .build();
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.saveAndFlush(therapistToSave));

    }
}
