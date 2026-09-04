package com.theraflow.service;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.exception.EmailDeliveryException;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.TokenExpiredException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.AuthService;
import com.theraflow.security.jwt.JWTService;
import com.theraflow.security.model.LoginRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final String EMAIL = "therapist@example.com";
    private static final String RAW_PASSWORD = "StrongPassword1!";
    private static final String PASSWORD_HASH = "encoded-password-hash";
    private static final String AUTH_TOKEN = "some-valid-token";
    private static final String VERIFICATION_TOKEN = "email-verification-token";
    private static final UUID ACCOUNT_ID =
            UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
    private static final Instant CREATED_AT = Instant.parse("2026-08-18T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-08-18T10:00:00Z");

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordValidator passwordValidator;
    @Mock
    private AuthService authService;
    @Mock
    private JWTService jwtService;
    @Mock
    private EmailService emailService;
    @Spy
    private DtoAccountMapper accountMapper;
    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_withValidRequest_returnsCreatedAccountResponse() {
        AccountRequest request = validAccountRequest();
        Account createdAccount = persistedAccount();

        AccountResponse expectedResponse = new AccountResponse(
                ACCOUNT_ID,
                EMAIL,
                AUTH_TOKEN,
                AccountType.THERAPIST,
                CREATED_AT,
                UPDATED_AT
        );

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(jwtService.generateEmailVerificationToken(EMAIL)).thenReturn(VERIFICATION_TOKEN);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(createdAccount);
        when(authService.authenticate(new LoginRequest(EMAIL, RAW_PASSWORD))).thenReturn(AUTH_TOKEN);

        AccountResponse actual = accountService.createAccount(request);

        assertThat(actual).isEqualTo(expectedResponse);

        verify(passwordValidator).validate(RAW_PASSWORD);
        verify(jwtService).generateEmailVerificationToken(EMAIL);
        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(accountMapper).toAccount(
                EMAIL,
                PASSWORD_HASH,
                AccountType.THERAPIST,
                VERIFICATION_TOKEN
        );
        verify(accountMapper).toResponse(createdAccount, AUTH_TOKEN);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        InOrder sideEffects = inOrder(accountRepository, emailService, authService);
        sideEffects.verify(accountRepository).saveAndFlush(accountCaptor.capture());
        sideEffects.verify(emailService).sendVerificationEmail(EMAIL, VERIFICATION_TOKEN);
        sideEffects.verify(authService).authenticate(new LoginRequest(EMAIL, RAW_PASSWORD));

        Account accountToSave = accountCaptor.getValue();
        assertThat(accountToSave.getEmail()).isEqualTo(EMAIL);
        assertThat(accountToSave.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(accountToSave.getType()).isEqualTo(AccountType.THERAPIST);
        assertThat(accountToSave.getVerificationToken()).isEqualTo(VERIFICATION_TOKEN);
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
        verifyNoInteractions(
                passwordEncoder,
                jwtService,
                accountMapper,
                accountRepository,
                emailService,
                authService
        );
    }

    @Test
    void createAccount_whenEmailDeliveryFails_doesNotAuthenticate() {
        AccountRequest request = validAccountRequest();
        Account createdAccount = persistedAccount();
        EmailDeliveryException expectedException = new EmailDeliveryException(
                EMAIL,
                new RuntimeException("SMTP unavailable")
        );

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(jwtService.generateEmailVerificationToken(EMAIL)).thenReturn(VERIFICATION_TOKEN);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(createdAccount);
        doThrow(expectedException)
                .when(emailService)
                .sendVerificationEmail(EMAIL, VERIFICATION_TOKEN);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isSameAs(expectedException);

        verify(accountRepository).saveAndFlush(any(Account.class));
        verify(emailService).sendVerificationEmail(EMAIL, VERIFICATION_TOKEN);
        verifyNoInteractions(authService);
    }

    @Test
    void changePassword_withCorrectOldPassword_updatesPasswordHash() {
        String oldPassword = "OldPassword1!";
        String currentPasswordHash = "current-password-hash";
        String newPassword = "NewPassword1!";
        String newPasswordHash = "new-password-hash";
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .passwordHash(currentPasswordHash)
                .build();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(oldPassword, currentPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPasswordHash);

        accountService.changePassword(request, ACCOUNT_ID);

        assertThat(account.getPasswordHash()).isEqualTo(newPasswordHash);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(passwordEncoder).matches(oldPassword, currentPasswordHash);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void changePassword_withIncorrectOldPassword_throwsBadCredentialsException() {
        String incorrectOldPassword = "WrongPassword1!";
        String currentPasswordHash = "current-password-hash";
        ChangePasswordRequest request = new ChangePasswordRequest(
                incorrectOldPassword,
                "NewPassword1!");
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .passwordHash(currentPasswordHash)
                .build();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(incorrectOldPassword, currentPasswordHash)).thenReturn(false);

        assertThatThrownBy(() -> accountService.changePassword(request, ACCOUNT_ID))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("The old password does not match your current password");

        assertThat(account.getPasswordHash()).isEqualTo(currentPasswordHash);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(passwordEncoder).matches(incorrectOldPassword, currentPasswordHash);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void verifyEmail_withExpiredToken_throwsTokenExpiredException() {
        ExpiredJwtException expiredJwtException =
                new ExpiredJwtException(null, null, "Token expired");

        when(jwtService.extractEmailFromVerificationToken(VERIFICATION_TOKEN))
                .thenThrow(expiredJwtException);

        assertThatThrownBy(() -> accountService.verifyEmail(VERIFICATION_TOKEN))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("The email verification link has expired. Please request a new one.");

        verifyNoInteractions(accountRepository);
    }

    @Test
    void verifyEmail_whenAccountDoesNotExist_throwsEntityNotFoundException() {
        when(jwtService.extractEmailFromVerificationToken(VERIFICATION_TOKEN)).thenReturn(EMAIL);
        when(accountRepository.findAccountByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.verifyEmail(VERIFICATION_TOKEN))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Account with email %s not found.", EMAIL);

        verify(jwtService).extractEmailFromVerificationToken(VERIFICATION_TOKEN);
        verify(accountRepository).findAccountByEmail(EMAIL);
    }

    private AccountRequest validAccountRequest() {
        return new AccountRequest(EMAIL, RAW_PASSWORD, AccountType.THERAPIST);
    }

    private Account persistedAccount() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .email(EMAIL)
                .passwordHash(PASSWORD_HASH)
                .verificationToken(VERIFICATION_TOKEN)
                .verified(false)
                .type(AccountType.THERAPIST)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }
}
