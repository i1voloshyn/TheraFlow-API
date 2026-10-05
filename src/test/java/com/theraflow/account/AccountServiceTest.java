package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtEmailVerificationTokenProvider;
import com.theraflow.config.PasswordLengthProperties;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.ResourceConflictException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final UUID ACCOUNT_ID =
            UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");

    @Spy
    private final PasswordValidator passwordValidator =
            new PasswordValidator(new PasswordLengthProperties(10, 20));

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtEmailVerificationTokenProvider jwtEmailService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private DtoAccountMapper accountMapper;
    @InjectMocks
    private AccountService accountService;

    @DisplayName("Sign up with existing email throws ResourceConflictException")
    @Test
    void signUp_error1() {
        AccountRequest request = new AccountRequest(
                "therapist@example.com",
                "Valid-password",
                AccountType.THERAPIST);

        when(accountRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> accountService.signUp(request))
                .isInstanceOf(ResourceConflictException.class)
                .satisfies(exception -> assertThat(((ResourceConflictException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));

        verify(accountRepository).existsByEmail(request.email());
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(
                passwordValidator,
                passwordEncoder,
                jwtEmailService,
                accountMapper,
                eventPublisher
        );

    }

    @DisplayName("Sign up with weak password throws PasswordPolicyException")
    @Test
    void signUp_error2() {
        String rawPassword = "WeakPassword";
        AccountRequest request = new AccountRequest(
                "therapist@example.com",
                rawPassword,
                AccountType.THERAPIST);

        assertThatThrownBy(() -> accountService.signUp(request))
                .isInstanceOf(PasswordPolicyException.class)
                .satisfies(exception -> assertThat(((PasswordPolicyException) exception).getViolations())
                        .containsExactlyInAnyOrder(
                                PasswordViolation.MISSING_SPECIAL_CHARACTER,
                                PasswordViolation.MISSING_NUMBER
                        ));

        verify(accountRepository).existsByEmail(request.email());
        verify(passwordValidator).validate(rawPassword);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(
                passwordEncoder,
                jwtEmailService,
                accountMapper,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Change password with correct old password updates password hash")
    void changePassword_success() {
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

    @DisplayName("Change password with incorrect old password throws InvalidCredentialsException")
    @Test
    void changePassword_error1() {
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
                .isInstanceOf(InvalidCredentialsException.class)
                .satisfies(exception -> assertThat(((InvalidCredentialsException) exception)
                        .getErrorCode()).isEqualTo(ErrorCode.PASSWORD_INCORRECT));

        assertThat(account.getPasswordHash()).isEqualTo(currentPasswordHash);
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(passwordEncoder).matches(incorrectOldPassword, currentPasswordHash);
        verify(passwordEncoder, never()).encode(any());
    }

    @DisplayName("Change password with new password same as old password throws ResourceConflictException")
    @Test
    void changePassword_error2() {
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
                .isInstanceOf(ResourceConflictException.class)
                .satisfies(exception -> assertThat(((ResourceConflictException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.PASSWORD_SAME_AS_OLD));

        verify(passwordValidator).validate(currentPassword);
        verify(passwordEncoder, never()).encode(any());
    }

    @DisplayName("""
            Verified email with expired tokens
            Should thrown InvalidCredentialsException
            With EMAIL_VERIFICATION_LINK_EXPIRED error code
            """)
    @Test
    void verifyEmail_error1() {
        String validVerificationToken = "cc837471-3c4b-4d77-a825-c4c1cf3a1dc5";

        when(jwtEmailService.extractEmail(validVerificationToken))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        assertThatThrownBy(() -> accountService.verifyEmail(validVerificationToken))
                .isInstanceOf(InvalidCredentialsException.class)
                .satisfies(exception ->
                        assertThat(((InvalidCredentialsException) exception).getErrorCode())
                                .isEqualTo(ErrorCode.EMAIL_VERIFICATION_LINK_EXPIRED));

        verifyNoInteractions(accountRepository);
    }

    @DisplayName("""
            Verified email with valid tokens but no account found)
            Should thrown EntityNotFoundException
            With ACCOUNT_NOT_FOUND error code
            """)
    @Test
    void verifyEmail_error2() {
        String validVerificationToken = "cc837471-3c4b-4d77-a825-c4c1cf3a1dc5";
        String validEmail = "email";

        when(jwtEmailService.extractEmail(validVerificationToken)).thenReturn(validEmail);
        when(accountRepository.findAccountByEmail(validEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.verifyEmail(validVerificationToken))
                .isInstanceOf(EntityNotFoundException.class)
                .satisfies(exception -> assertThat(((EntityNotFoundException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));

        verify(jwtEmailService).extractEmail(validVerificationToken);
        verify(accountRepository).findAccountByEmail(validEmail);
    }

}
