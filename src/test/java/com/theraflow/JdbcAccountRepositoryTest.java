package com.theraflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@Import(value = {JdbcAccountRepository.class, TestcontainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcAccountRepositoryTest {
    @Autowired
    AccountRepository repository;
    @Autowired
    JdbcTemplate template;

    @Sql("/fixtures/accounts/accounts_clean_up.sql")
    @Test
    void save_shouldSaveAndReturnNewAccount() {
        String countAllAccountsQuery = """
                SELECT COUNT(*) FROM accounts
                """;
        Account accountToSave = new Account("test@email", "passwordHash", AccountType.THERAPIST);

        Account actual = repository.save(accountToSave);
        Long quantity = template.queryForObject(countAllAccountsQuery, Long.class);

        assertThat(quantity).isOne();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getEmail()).isEqualTo(accountToSave.getEmail());
    }

    @Sql("/fixtures/accounts/accounts_clean_up.sql")
    @Test
    void save_shouldThrowException_whenEmailAlreadyExistInDataBase() {
        Account duplicateAccount = new Account("test@email", "different_hash", AccountType.THERAPIST);
        String insertTestAccountQuery = """
                     INSERT INTO accounts(email, password_hash, account_type)
                VALUES ('test@email', 'password_hash', 'therapist')
                """;
        template.update(insertTestAccountQuery);

        assertThatExceptionOfType(DuplicateKeyException.class)
                .isThrownBy(() -> repository.save(duplicateAccount));
    }

}