package com.theraflow.authentication;

import com.theraflow.authentication.dto.AccountRequest;
import com.theraflow.authentication.model.Account;
import com.theraflow.authentication.model.AccountType;
import com.theraflow.application.JwtAuthTokenProvider;
import com.theraflow.application.JwtEmailVerificationTokenProvider;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.application.refreshToken.UuidTokenProvider;
import com.theraflow.authentication.dto.ConfirmPasswordResetRequest;
import com.theraflow.authentication.dto.JwtPair;
import com.theraflow.authentication.dto.LoginRequest;
import com.theraflow.authentication.model.PasswordResetToken;
import com.theraflow.config.PasswordLengthProperties;
import com.theraflow.event.PasswordResetRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.ResourceConflictException;
import com.theraflow.exception.TheraflowApiException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import io.jsonwebtoken.ExpiredJwtException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID RANDOM_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String VALID_PASSWORD = "!1ValidPassword";
    private static final String WEAK_PASSWORD = "WeakPassword";

    @Spy
    private Clock clock = Clock.systemUTC();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Supplier<UUID> uuidSupplier;

    @Mock
    private JwtAuthTokenProvider jwtAuthTokenProvider;

    @Mock
    private UuidTokenProvider uuidTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private PasswordValidator passwordValidator = new PasswordValidator(new PasswordLengthProperties(10, 32));

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private JwtEmailVerificationTokenProvider jwtEmailService;
    @Spy
    private DtoAccountMapper accountMapper;


    @DisplayName("Sign up with existing email throws ResourceConflictException")
    @Test
    void signUp_error1() {
        AccountRequest request = new AccountRequest(
                "therapist@example.com",
                VALID_PASSWORD,
                AccountType.THERAPIST);

        when(accountRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.signUp(request))
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
        AccountRequest request = new AccountRequest(
                "therapist@example.com",
                WEAK_PASSWORD,
                AccountType.THERAPIST);

        when(accountRepository.existsByEmail(request.email())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.signUp(request))
                .isInstanceOf(PasswordPolicyException.class)
                .satisfies(exception -> assertThat(((PasswordPolicyException) exception).getViolations())
                        .containsExactlyInAnyOrder(
                                PasswordViolation.MISSING_SPECIAL_CHARACTER,
                                PasswordViolation.MISSING_NUMBER
                        ));

        verify(accountRepository).existsByEmail(request.email());
        verify(passwordValidator).validate(WEAK_PASSWORD);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(
                passwordEncoder,
                jwtEmailService,
                accountMapper,
                eventPublisher
        );
    }

    @DisplayName("Should return a valid JwtPair for valid credentials")
    @Test
    void authenticate_success() {
        LoginRequest request = loginRequest();
        String accessToken = "access-tokens";
        String uuidRefreshToken = "123e4567-e89b-12d3-a456-426614174000";
        Authentication authResponse = new AuthenticationImpl();
        Authentication authRequest = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        );
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .passwordHash("hashed-password")
                .build();
        RefreshToken expectedRefreshToken = new RefreshToken(
                "hashed-refresh-tokens",
                clock.instant().plusSeconds(3600));

        JwtPair expected = new JwtPair(accessToken, uuidRefreshToken);

        when(authenticationManager.authenticate(authRequest)).thenReturn(authResponse);
        when(accountRepository.isEmailVerified(request.email())).thenReturn(true);
        when(jwtAuthTokenProvider.generateAccessToken(any(TheraflowUser.class))).thenReturn(accessToken);
        when(uuidSupplier.get()).thenReturn(UUID.fromString(uuidRefreshToken));
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(uuidTokenProvider.buildRefreshToken(uuidRefreshToken)).thenReturn(expectedRefreshToken);

        JwtPair actual = authenticationService.authenticate(request);

        verify(authenticationManager).authenticate(authRequest);
        verify(accountRepository).isEmailVerified(request.email());
        verify(jwtAuthTokenProvider).generateAccessToken(any(TheraflowUser.class));
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(uuidTokenProvider).buildRefreshToken(any(String.class));

        assertThat(actual.access()).isEqualTo(expected.access());
        assertThat(actual.refresh()).isEqualTo(expected.refresh());

        assertThat(account.getRefreshTokens()).containsExactly(expectedRefreshToken);
    }

    @DisplayName("Should throw a PermissionException for an account with an unverified email status")
    @Test
    void authenticate_throwsException1() {
        LoginRequest request = loginRequest();

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                        request.password()))).thenReturn(any());

        when(accountRepository.isEmailVerified(request.email())).thenReturn(false);

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.authenticate(request))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.EMAIL_NOT_VERIFIED));

        verifyNoInteractions(jwtAuthTokenProvider, uuidTokenProvider);
    }

    @DisplayName("Should throw an InvalidCredentialsException for a request with an incorrect email")
    @Test
    void authenticate_throwsException2() {
        LoginRequest invalidRequest = loginRequestWithBadEmail();
        doThrow(new InvalidCredentialsException(ErrorCode.AUTHENTICATION_FAILED))
                .when(authenticationManager).authenticate(
                        new UsernamePasswordAuthenticationToken(invalidRequest.email(),
                                invalidRequest.password()));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> authenticationService.authenticate(invalidRequest))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILED));

        verifyNoInteractions(jwtAuthTokenProvider, uuidTokenProvider, accountRepository);
    }

    @DisplayName("Should throw an exception when refresh tokens is expired")
    @Test
    void rotateToken_throwsWhenTokenExpired() {
        String rawToken = "123e4567-e89b-12d3-a456-426614174000";
        String rawTokenHash = "hashed-expired-refresh-token";
        RefreshToken oldRefreshToken = new RefreshToken();
        oldRefreshToken.setTokenHash(rawTokenHash);
        oldRefreshToken.setIsRevoked(false);
        oldRefreshToken.setExpiresAt(clock.instant().minusSeconds(1));

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(rawTokenHash);
        when(refreshTokenRepository.findByTokenHash(rawTokenHash)).thenReturn(Optional.of(oldRefreshToken));

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.rotateTokens(rawToken))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.REFRESH_TOKEN_EXPIRED));

        verifyNoInteractions(accountRepository, jwtAuthTokenProvider, authenticationManager);
    }

    @DisplayName("""
            rotateToken
            Should thrown an exception with
            REFRESH_TOKEN_REVOKED code
            WHEN refresh tokens is revoked
            """)
    @Test
    void rotateToken_throwsWhenTokenRevoked() {
        String rawToken = "123e4567-e89b-12d3-a456-426614174000";
        String rawTokenHash = uuidTokenProvider.hashToken(rawToken);
        RefreshToken oldRefreshToken = new RefreshToken();
        oldRefreshToken.setTokenHash(rawTokenHash);
        oldRefreshToken.setIsRevoked(true);

        when(refreshTokenRepository.findByTokenHash(rawTokenHash)).thenReturn(Optional.of(oldRefreshToken));

        assertThatExceptionOfType(TheraflowApiException.class)
                .isThrownBy(() -> authenticationService.rotateTokens(rawToken))
                .matches(ex -> ex.getErrorCode().name().equals(ErrorCode.REFRESH_TOKEN_REVOKED.name()));

        verifyNoInteractions(accountRepository, jwtAuthTokenProvider, authenticationManager);
    }

    @DisplayName("""
            requestPasswordReset
            SHOULD NOT create a token
            AND SHOULD NOT send an email
            WHEN the email does not exist in the database
            """)
    @Test
    public void requestPasswordReset_doNothing() {
        String nonExistentEmail = "wrong-email";

        when(accountRepository.findAccountByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        authenticationService.requestPasswordReset(nonExistentEmail);

        verifyNoInteractions(uuidSupplier, uuidTokenProvider, eventPublisher);
    }

    @DisplayName("""
            requestPasswordReset
            SHOULD create resetToken
            AND SHOULD send an email
            WHEN the email exists in the database
            """)
    @Test
    public void requestPasswordReset_success() {
        Account account = new Account();
        String existingEmail = "valid-email";
        String rawResetToken = RANDOM_UUID.toString();
        PasswordResetToken resetToken = new PasswordResetToken("token-hash",
                clock.instant().plusSeconds(3600));

        PasswordResetRequest resetEventRequest = new PasswordResetRequest(
                existingEmail,
                rawResetToken
        );

        when(accountRepository.findAccountByEmail(existingEmail)).thenReturn(Optional.of(account));
        when(uuidSupplier.get()).thenReturn(RANDOM_UUID);
        when(uuidTokenProvider.buildPasswordResetToken(rawResetToken)).thenReturn(resetToken);

        authenticationService.requestPasswordReset(existingEmail);

        verify(accountRepository).findAccountByEmail(existingEmail);
        verify(uuidSupplier).get();
        verify(uuidTokenProvider).buildPasswordResetToken(rawResetToken);
        verify(eventPublisher).publishEvent(resetEventRequest);

        assertThat(account.getPasswordResetTokens())
                .containsExactly(resetToken);
    }

    @DisplayName("""
            verifyPasswordReset
            SHOULD validate token
            WHEN the token is valid and not used
            """)
    @Test
    void verifyPasswordReset_success() {
        String rawToken = "raw-reset-token";
        String hashedToken = "hashed-reset-token";
        Account account = new Account();

        PasswordResetToken resetToken = new PasswordResetToken(hashedToken, clock.instant().plusSeconds(3600));
        resetToken.setAccount(account);
        resetToken.setUsed(false);

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(resetToken));

        authenticationService.verifyPasswordReset(rawToken);

        verify(uuidTokenProvider).hashToken(rawToken);
        verify(passwordResetTokenRepository).findByTokenHash(hashedToken);
    }

    @DisplayName("""
            confirmPasswordReset
            SHOULD update password and mark token used
            WHEN token is valid and passwords match
            """)
    @Test
    void confirmPasswordReset_success() {
        String rawToken = "raw-reset-token";
        String hashedToken = "hashed-reset-token";
        String encodedPassword = "encoded-password";
        Account account = Account.builder()
                .passwordHash("old-password-hash")
                .build();
        PasswordResetToken resetToken = new PasswordResetToken(hashedToken, clock.instant().plusSeconds(3600));
        resetToken.setAccount(account);
        resetToken.setUsed(false);
        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest(rawToken, VALID_PASSWORD, VALID_PASSWORD  );

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(VALID_PASSWORD  )).thenReturn(encodedPassword);

        authenticationService.confirmPasswordReset(request);

        verify(passwordValidator).validate(VALID_PASSWORD);
        verify(passwordEncoder).encode(VALID_PASSWORD);
        assertThat(account.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(resetToken.isUsed()).isTrue();
    }

    @DisplayName("""
            confirmPasswordReset
            SHOULD throw PermissionException
            WITH PASSWORD_NOT_MATCH
            WHEN passwords are different
            """)
    @Test
    void confirmPasswordReset_throwsWhenPasswordsDoNotMatch() {
        String rawToken = "raw-reset-token";
        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest(
                rawToken,
                VALID_PASSWORD,
                VALID_PASSWORD + "mismatch"
        );

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.confirmPasswordReset(request))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.PASSWORD_NOT_MATCH));

        verify(passwordValidator).validate(VALID_PASSWORD);
        verifyNoInteractions(uuidTokenProvider, passwordResetTokenRepository, passwordEncoder);
    }

    @DisplayName("""
            confirmPasswordReset
            SHOULD throw PermissionException
            WITH PASSWORD_RESET_TOKEN_INVALID
            WHEN token is expired
            """)
    @Test
    void confirmPasswordReset_throwsWhenTokenExpired() {
        String rawToken = "expired-token";
        String hashedToken = "hashed-expired-token";
        String newPassword = "new-password";
        PasswordResetToken expiredResetToken = new PasswordResetToken(
                hashedToken,
                clock.instant().minusSeconds(1)
        );
        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest(rawToken, VALID_PASSWORD, VALID_PASSWORD);

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(expiredResetToken));

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.confirmPasswordReset(request))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        verify(passwordValidator).validate(VALID_PASSWORD);
        verify(uuidTokenProvider).hashToken(rawToken);
        verify(passwordResetTokenRepository).findByTokenHash(hashedToken);
        verifyNoInteractions(passwordEncoder);
    }

    @DisplayName("""
            verifyPasswordReset
            SHOULD throw PermissionException
            WITH PASSWORD_RESET_TOKEN_INVALID
            WHEN token hash is not found
            """)
    @Test
    void verifyPasswordReset_throwsWhenTokenNotFound() {
        String rawToken = "missing-token";
        String hashedToken = "hashed-missing-token";

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.verifyPasswordReset(rawToken))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));
    }

    @DisplayName("""
            verifyPasswordReset
            SHOULD throw PermissionException
            WITH PASSWORD_RESET_TOKEN_INVALID
            WHEN token is already used
            """)
    @Test
    void verifyPasswordReset_throwsWhenTokenUsed() {
        String rawToken = "used-token";
        String hashedToken = "hashed-used-token";
        PasswordResetToken resetToken = new PasswordResetToken(hashedToken, clock.instant().plusSeconds(3600));
        resetToken.setUsed(true);

        when(uuidTokenProvider.hashToken(rawToken)).thenReturn(hashedToken);
        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(resetToken));

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.verifyPasswordReset(rawToken))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));
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

        authenticationService.changePassword(request, ACCOUNT_ID);

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

        assertThatThrownBy(() -> authenticationService.changePassword(request, ACCOUNT_ID))
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

        assertThatThrownBy(() -> authenticationService.changePassword(request, ACCOUNT_ID))
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

        assertThatThrownBy(() -> authenticationService.verifyEmail(validVerificationToken))
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

        assertThatThrownBy(() -> authenticationService.verifyEmail(validVerificationToken))
                .isInstanceOf(EntityNotFoundException.class)
                .satisfies(exception -> assertThat(((EntityNotFoundException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));

        verify(jwtEmailService).extractEmail(validVerificationToken);
        verify(accountRepository).findAccountByEmail(validEmail);
    }


    private LoginRequest loginRequest() {
        return new LoginRequest("valid-email", "valid-password");
    }

    private LoginRequest loginRequestWithBadEmail() {
        return new LoginRequest("valid-email", "valid-password");
    }

    private class AuthenticationImpl implements Authentication {

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of();
        }

        @Override
        public @Nullable Object getCredentials() {
            return null;
        }

        @Override
        public @Nullable Object getDetails() {
            return null;
        }

        @Override
        public @Nullable Object getPrincipal() {
            return theraflowUser();
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return "";
        }
    }

    private TheraflowUser theraflowUser() {
        return new TheraflowUser(ACCOUNT_ID,
                "valid-email",
                "valid-password",
                true,
                AccountType.THERAPIST);
    }

}