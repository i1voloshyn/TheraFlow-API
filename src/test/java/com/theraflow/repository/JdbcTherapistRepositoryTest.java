package com.theraflow.repository;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.model.Therapist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class JdbcTherapistRepositoryTest {

//    @Autowired
//    TherapistRepository repository;
//    @Autowired
//    JdbcTemplate template;
//
//    @Sql("/fixtures/therapists/therapists_clean_up.sql")
//    @Test
//    void createProfile_shouldCreate_andReturnSuccessfully_createdTherapistProfile() {
//        UUID accountId = insertTestAccount();
//        String countAllTherapistsQuery = """
//                SELECT COUNT(*) FROM therapists
//                """;
//
//        Therapist therapistToSave = Therapist.builder()
//                .accountId(accountId)
//                .firstName("Jeremiah")
//                .lastName("Nevada")
//                .licenseNumber("RTF 5456")
//                .professionalTitle("Doctor")
//                .bio("Just therapist")
//                .build();
//
//        Therapist actual = repository.createProfile(therapistToSave);
//        Long quantity = template.queryForObject(countAllTherapistsQuery, Long.class);
//
//        assertThat(quantity).isOne();
//        assertThat(actual.getId()).isNotNull();
//    }
//
//    @Sql("/fixtures/therapists/therapists_clean_up.sql")
//    @Test
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    void createProfile_shouldThrownException_forInvalidAccountId() {
//        String countAllTherapistsQuery = """
//                SELECT COUNT(*) FROM therapists
//                """;
//
//        UUID randomId = UUID.randomUUID();
//
//        Therapist therapistToSave = Therapist.builder()
//                .accountId(randomId)
//                .firstName("Jeremiah")
//                .lastName("Nevada")
//                .licenseNumber("RTF 5456")
//                .professionalTitle("Doctor")
//                .bio("Just therapist")
//                .build();
//        assertThatExceptionOfType(DataIntegrityViolationException.class)
//                .isThrownBy(() -> repository.createProfile(therapistToSave));
//
//        Long quantity = template.queryForObject(countAllTherapistsQuery, Long.class);
//
//        assertThat(quantity).isZero();
//    }
//
//    @Sql("/fixtures/therapists/therapists_clean_up.sql")
//    @Test
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    void updateProfile_shouldSuccessfullyUpdateProfile_forExactTherapistId() {
//        UUID accountId = insertTestAccount();
//
//        Therapist therapistToSave = Therapist.builder()
//                .accountId(accountId)
//                .firstName("Jeremiah")
//                .lastName("Nevada")
//                .licenseNumber("RTF 5456")
//                .professionalTitle("Doctor")
//                .bio("Just therapist")
//                .build();
//
//        Therapist actual = repository.createProfile(therapistToSave);
//
//
//        String changedProfTitle = "Super Doctor";
//
//        Therapist therapistToUpdate = Therapist.builder()
//                .id(actual.getId())
//                .firstName(actual.getFirstName())
//                .lastName(actual.getLastName())
//                .professionalTitle(changedProfTitle)
//                .bio(actual.getBio())
//                .build();
//        Therapist updated = repository.updateProfile(therapistToUpdate);
//
//        assertThat(updated.getId()).isEqualTo(actual.getId());
//        assertThat(updated.getProfessionalTitle()).isEqualTo(changedProfTitle);
//        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
//
//    }
//
//    private UUID insertTestAccount() {
//        String insertAccountWithReturningId = """
//                INSERT INTO accounts (email, password_hash, account_type)
//                VALUES ('test@email', 'random_hash', 'therapist')
//                RETURNING id;
//                """;
//        return template.queryForObject(insertAccountWithReturningId, UUID.class);
//    }
//

}
