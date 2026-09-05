package com.theraflow.service;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.TokenExpiredException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.AuthService;
import com.theraflow.security.jwt.JWTService;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final String EMAIL = "therapist@example.com";
    private static final String VERIFICATION_TOKEN = "email-verification-token";
    private static final UUID ACCOUNT_ID =
            UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");

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
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private DtoAccountMapper accountMapper;
    @InjectMocks
    private AccountService accountService;

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
                eventPublisher,
                authService
        );
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
        verify(passwordEncoder).matches(newPassword, currentPasswordHash);
        verify(passwordValidator).validate(newPassword);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void changePassword_withIncorrectOldPassword_throwsCurrentPasswordMismatchException() {
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
                .isInstanceOf(CurrentPasswordMismatchException.class)
                .hasMessage("The old password does not match your current password");

        assertThat(account.getPasswordHash()).isEqualTo(currentPasswordHash);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(passwordEncoder).matches(incorrectOldPassword, currentPasswordHash);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_withCurrentPasswordAsNewPassword_rejectsPasswordReuse() {
        String currentPassword = "CurrentPassword1!";
        String currentPasswordHash = "current-password-hash";
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, currentPassword);
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .passwordHash(currentPasswordHash)
                .build();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(currentPassword, currentPasswordHash)).thenReturn(true);

        assertThatThrownBy(() -> accountService.changePassword(request, ACCOUNT_ID))
                .isInstanceOf(PasswordPolicyException.class)
                .satisfies(exception -> assertThat(((PasswordPolicyException) exception).getViolations())
                        .containsExactly(PasswordViolation.SAME_AS_CURRENT));

        verifyNoInteractions(passwordValidator);
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

}
