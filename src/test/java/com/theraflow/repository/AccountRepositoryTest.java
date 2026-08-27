package com.theraflow.repository;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {
    @Autowired
    AccountRepository repository;
    @Autowired
    EntityManager entityManager;

    @Test
    void save_shouldPersistAndReturnAccount() {
        Account accountToSave = Account.builder()
                .email("test@example.com")
                .passwordHash("passwordHash")
                .type(AccountType.THERAPIST)
                .build();

        Account saved = repository.saveAndFlush(accountToSave);
        entityManager.clear();

       // Account actual = repository.findById(saved.getId()).orElseThrow();

        assertThat(repository.count()).isOne();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("passwordHash");
        assertThat(saved.getType()).isEqualTo(AccountType.THERAPIST);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_shouldThrowException_whenEmailAlreadyExists() {
        Account existingAccount = Account.builder()
                .email("duplicate@example.com")
                .passwordHash("firstPasswordHash")
                .type(AccountType.THERAPIST)
                .build();
        repository.saveAndFlush(existingAccount);

        Account duplicateAccount = Account.builder()
                .email("duplicate@example.com")
                .passwordHash("secondPasswordHash")
                .type(AccountType.GUARDIAN)
                .build();

        assertThatThrownBy(() -> repository.saveAndFlush(duplicateAccount))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
