package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.jwt.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties =
        "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=")
class AccountServiceIntegrationTest {
    private static final String EMAIL = "therapist@example.com";
    private static final String RAW_PASSWORD = "StrongPassword1!";

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTService jwtService;

    @Test
    void createAccount_withValidRequest_persistsUnverifiedAccountAndReturnsAccessToken() {
        AccountResponse response = accountService.createAccount(
                new AccountRequest(EMAIL, RAW_PASSWORD, AccountType.THERAPIST)
        );

        Account savedAccount = accountRepository.findAccountByEmail(EMAIL).orElseThrow();

        assertThat(savedAccount.getId()).isEqualTo(response.id());
        assertThat(savedAccount.getEmail()).isEqualTo(EMAIL);
        assertThat(savedAccount.getType()).isEqualTo(AccountType.THERAPIST);
        assertThat(savedAccount.getVerified()).isFalse();
        assertThat(savedAccount.getVerificationToken()).isNotBlank();
        assertThat(passwordEncoder.matches(RAW_PASSWORD, savedAccount.getPasswordHash())).isTrue();
        assertThat(jwtService.extractEmailFromVerificationToken(savedAccount.getVerificationToken()))
                .isEqualTo(EMAIL);

        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.type()).isEqualTo(AccountType.THERAPIST);
        assertThat(response.token()).isNotBlank();
        assertThat(jwtService.extractUsernameFromAccessToken(response.token())).isEqualTo(EMAIL);
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }
}
