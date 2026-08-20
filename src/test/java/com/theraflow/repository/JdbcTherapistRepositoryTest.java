package com.theraflow.repository;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.model.Therapist;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Import(value = {JdbcTherapistRepository.class, TestcontainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@JdbcTest
@Transactional()
class JdbcTherapistRepositoryTest {

    @Autowired
    TherapistRepository repository;
    @Autowired
    JdbcTemplate template;

    @Sql("/fixtures/therapists/therapists_clean_up.sql")
    @Test
    void createProfile_shouldCreate_andReturnSuccessfully_createdTherapistProfile() {
        String insertAccountWithReturningId = """
                INSERT INTO accounts (email, password_hash, account_type)
                VALUES ('test@email', 'random_hash', 'therapist')
                RETURNING id;
                """;

        String countAllTherapistsQuery = """
                SELECT COUNT(*) FROM therapists
                """;

        UUID id = template.queryForObject(insertAccountWithReturningId, UUID.class);

        Therapist therapistToSave = new Therapist(
                id,
                "Jeremiah",
                "Nevada",
                "RTF 5456",
                "Doctor",
                "Just therapist"
        );

        Therapist actual = repository.createProfile(therapistToSave);
        Long quantity = template.queryForObject(countAllTherapistsQuery, Long.class);

        assertThat(quantity).isOne();
        assertThat(actual.getId()).isNotNull();
    }

    @Sql("/fixtures/therapists/therapists_clean_up.sql")
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createProfile_shouldThrownException_forInvalidAccountId() {
        String countAllTherapistsQuery = """
                SELECT COUNT(*) FROM therapists
                """;

        UUID randomId = UUID.randomUUID();

        Therapist therapistToSave = new Therapist(
                randomId,
                "Jeremiah",
                "Nevada",
                "RTF 5456",
                "Doctor",
                "Just therapist"
        );
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> repository.createProfile(therapistToSave));

        Long quantity = template.queryForObject(countAllTherapistsQuery, Long.class);

        assertThat(quantity).isZero();
    }


}