package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.authentication.AuthenticationService;
import com.theraflow.config.PasswordLengthProperties;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.ResourceConflictException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
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
    private AuthenticationService authService;
    @Mock
    private JwtAuthTokenService jwtService;
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
                jwtService,
                accountMapper,
                eventPublisher,
                authService
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
                jwtService,
                accountMapper,
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
                .isInstanceOf(ResourceConflictException.class)
                .satisfies(exception -> assertThat(((ResourceConflictException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.PASSWORD_SAME_AS_OLD));

        verify(passwordValidator).validate(currentPassword);
        verify(passwordEncoder, never()).encode(any());
    }
//
//    @Test
//    void verifyEmail_withExpiredToken_throwsTokenExpiredException() {
//        ExpiredJwtException expiredJwtException =
//                new ExpiredJwtException(null, null, "Token expired");
//
//        when(jwtService.extractEmailFromVerificationToken(VERIFICATION_TOKEN))
//                .thenThrow(expiredJwtException);
//
//        assertThatThrownBy(() -> accountService.verifyEmail(VERIFICATION_TOKEN))
//                .isInstanceOf(TokenExpiredException.class)
//                .hasMessage("The email verification link has expired. Please request a new one.");
//
//        verifyNoInteractions(accountRepository);
//    }
//
//    @Test
//    void verifyEmail_whenAccountDoesNotExist_throwsEntityNotFoundException() {
//        when(jwtService.extractEmailFromVerificationToken(VERIFICATION_TOKEN)).thenReturn(EMAIL);
//        when(accountRepository.findAccountByEmail(EMAIL)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> accountService.verifyEmail(VERIFICATION_TOKEN))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessage("Account with email %s not found.", EMAIL);
//
//        verify(jwtService).extractEmailFromVerificationToken(VERIFICATION_TOKEN);
//        verify(accountRepository).findAccountByEmail(EMAIL);
//    }

}
