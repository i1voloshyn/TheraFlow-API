package com.theraflow.service;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.AuthService;
import com.theraflow.security.model.LoginRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    //rewrite it as integration
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;
    @Mock
    private AuthService authService;
    @Mock
    private EmailService emailService;

    @Spy
    private DtoAccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;


    @Test
    void createAccount_withValidRequest_returnsCreatedAccountResponse() {
        String email = "therapist@example.com";
        String rawPassword = "StrongPassword1!";
        String passwordHash = "encoded-password-hash";
        String token = "some-valid-token";
        AccountType type = AccountType.THERAPIST;
        AccountRequest request = new AccountRequest(email, rawPassword, type);

        LoginRequest loginRequest = new LoginRequest(email, rawPassword);

        Account accountToSave = Account.builder()
                .id(null)
                .email(email)
                .passwordHash(passwordHash)
                .type(type)
                .createdAt(null)
                .updatedAt(null)
                .build();

        UUID id = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        Instant createdAt = Instant.parse("2026-08-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-18T10:00:00Z");

        Account createdAccount = Account.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .type(type)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountResponse expectedResponse = new AccountResponse(
                id,
                email,
                token,
                type,
                createdAt,
                updatedAt);

        when(passwordEncoder.encode(rawPassword)).thenReturn(passwordHash);
        // doNothing().when(emailService).sendEmailConfirmationEmail(email);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(createdAccount);
        when(authService.authenticate(loginRequest)).thenReturn(token);

        AccountResponse actual = accountService.createAccount(request);

        assertThat(actual).isEqualTo(expectedResponse);

        InOrder processingOrder = Mockito.inOrder(
                passwordValidator,
                passwordEncoder,
                accountMapper,
                accountRepository);

        processingOrder.verify(passwordValidator).validate(rawPassword);
        processingOrder.verify(passwordEncoder).encode(rawPassword);
        processingOrder.verify(accountMapper).toAccount(email, passwordHash, type);
        processingOrder.verify(accountRepository).saveAndFlush(any(Account.class));
        processingOrder.verify(accountMapper).toResponse(createdAccount, token);
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

    @Test
    void changePassword_withCorrectOldPassword_updatesPasswordHash() {
        UUID accountId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        String oldPassword = "OldPassword1!";
        String currentPasswordHash = "current-password-hash";
        String newPassword = "NewPassword1!";
        String newPasswordHash = "new-password-hash";
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        Account account = Account.builder()
                .id(accountId)
                .passwordHash(currentPasswordHash)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(oldPassword, currentPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPasswordHash);

        accountService.changePassword(request, accountId);

        assertThat(account.getPasswordHash()).isEqualTo(newPasswordHash);
        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).matches(oldPassword, currentPasswordHash);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void changePassword_withIncorrectOldPassword_throwsBadCredentialsException() {
        UUID accountId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        String incorrectOldPassword = "WrongPassword1!";
        String currentPasswordHash = "current-password-hash";
        ChangePasswordRequest request = new ChangePasswordRequest(
                incorrectOldPassword,
                "NewPassword1!");
        Account account = Account.builder()
                .id(accountId)
                .passwordHash(currentPasswordHash)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(incorrectOldPassword, currentPasswordHash)).thenReturn(false);

        assertThatThrownBy(() -> accountService.changePassword(request, accountId))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The old password does not match your current password");

        assertThat(account.getPasswordHash()).isEqualTo(currentPasswordHash);
        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).matches(incorrectOldPassword, currentPasswordHash);
        verify(passwordEncoder, never()).encode(any());
    }
}
