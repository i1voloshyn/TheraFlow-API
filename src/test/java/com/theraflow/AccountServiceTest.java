package com.theraflow;

import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.model.dto.AccountRequest;
import com.theraflow.model.dto.AccountResponse;
import com.theraflow.repository.AccountRepository;
import com.theraflow.service.AccountService;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @Spy
    private DtoAccountMapper accountMapper = new DtoAccountMapper();

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_withValidRequest_returnsCreatedAccountResponse() {
        String email = "therapist@example.com";
        String rawPassword = "StrongPassword1!";
        String passwordHash = "encoded-password-hash";
        AccountType type = AccountType.THERAPIST;
        AccountRequest request = new AccountRequest(email, rawPassword, type);

        Account accountToSave = new Account(null, email, passwordHash, type, null, null);

        UUID id = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        Instant createdAt = Instant.parse("2026-08-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-18T10:00:00Z");

        Account createdAccount = new Account(
                id,
                email,
                passwordHash,
                type,
                createdAt,
                updatedAt);

        AccountResponse expectedResponse = new AccountResponse(
                id,
                email,
                type,
                createdAt,
                updatedAt);

        when(passwordEncoder.encode(rawPassword)).thenReturn(passwordHash);
        when(accountRepository.create(accountToSave)).thenReturn(createdAccount);

        AccountResponse actual = accountService.createAccount(request);

        assertThat(actual).isEqualTo(expectedResponse);

        InOrder processingOrder = inOrder(
                passwordValidator,
                passwordEncoder,
                accountMapper,
                accountRepository);

        processingOrder.verify(passwordValidator).validate(rawPassword);
        processingOrder.verify(passwordEncoder).encode(rawPassword);
        processingOrder.verify(accountMapper).toAccount(email, passwordHash, type);
        processingOrder.verify(accountRepository).create(accountToSave);
        processingOrder.verify(accountMapper).toResponse(createdAccount);
    }

    @Test
    void createAccount_withInvalidPassword_doesNotCreateAccount() {
        String rawPassword = "WeakPassword";
        AccountRequest request = new AccountRequest(
                "therapist@example.com",
                rawPassword,
                AccountType.THERAPIST);

        PasswordPolicyException expectedException = new PasswordPolicyException(
                Set.of(PasswordViolation.MISSING_NUMBER, PasswordViolation.MISSING_SPECIAL_CHARACTER));

        doThrow(expectedException)
                .when(passwordValidator)
                .validate(rawPassword);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isSameAs(expectedException);

        verify(passwordValidator).validate(rawPassword);
        verifyNoInteractions(passwordEncoder, accountMapper, accountRepository);
    }
}
