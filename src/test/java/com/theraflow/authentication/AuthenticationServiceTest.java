package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtAuthTokenProvider;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.application.refreshToken.UuidTokenProvider;
import com.theraflow.authentication.dto.JwtPair;
import com.theraflow.authentication.dto.LoginRequest;
import com.theraflow.authentication.model.PasswordResetToken;
import com.theraflow.event.PasswordResetRequest;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.TheraflowApiException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID RANDOM_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    private final Clock clock = Clock.systemUTC();

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
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthenticationService authenticationService;

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

    @DisplayName("Should thrown an exception when refresh tokens is expired")
    @Test
    void rotateToken_sc1() {

    }

    @DisplayName("Should thrown an exception with REFRESH_TOKEN_REVOKED code when refresh tokens is revoked")
    @Test
    void rotateToken_sc2() {
        String rawToken = "123e4567-e89b-12d3-a456-426614174000";
        String rawTokenHash = uuidTokenProvider.hashRefreshToken(rawToken);
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