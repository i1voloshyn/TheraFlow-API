package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenService;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private final Clock clock = Clock.systemUTC();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Supplier<UUID> uuidSupplier;

    @Mock
    private JwtAuthTokenService jwtAuthTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_success() {
        LoginRequest request = loginRequest();
        String accessToken = "access-token";
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
                "hashed-refresh-token",
                clock.instant().plusSeconds(3600));

        AuthTokenPair expected = new AuthTokenPair(accessToken, uuidRefreshToken);

        when(authenticationManager.authenticate(authRequest)).thenReturn(authResponse);
        when(accountRepository.isEmailVerified(request.email())).thenReturn(true);
        when(jwtAuthTokenService.generateAccessToken(any(TheraflowUser.class))).thenReturn(accessToken);
        when(uuidSupplier.get()).thenReturn(UUID.fromString(uuidRefreshToken));
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(refreshTokenService.buildRefreshToken(uuidRefreshToken)).thenReturn(expectedRefreshToken);

        AuthTokenPair actual = authenticationService.authenticate(request);

        verify(authenticationManager).authenticate(authRequest);
        verify(accountRepository).isEmailVerified(request.email());
        verify(jwtAuthTokenService).generateAccessToken(any(TheraflowUser.class));
        verify(accountRepository).findById(ACCOUNT_ID);
        verify(refreshTokenService).buildRefreshToken(any(String.class));

        assertThat(actual.access()).isEqualTo(expected.access());
        assertThat(actual.refresh()).isEqualTo(expected.refresh());

        assertThat(account.getRefreshTokens()).containsExactly(expectedRefreshToken);
    }

    @DisplayName("Should throw PermissionException for account with unverified email status ")
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

        verifyNoInteractions(jwtAuthTokenService, refreshTokenService);
    }

    @DisplayName("Should throw InvalidCredentialsException for invalid credentials")
    @Test
    void authenticate_throwsException2() {
        LoginRequest invalidRequest = loginRequest();
        doThrow(new InvalidCredentialsException(ErrorCode.AUTHENTICATION_FAILED))
                .when(authenticationManager).authenticate(
                        new UsernamePasswordAuthenticationToken(invalidRequest.email(),
                                invalidRequest.password()));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> authenticationService.authenticate(invalidRequest))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.AUTHENTICATION_FAILED));

        verifyNoInteractions(jwtAuthTokenService, refreshTokenService, accountRepository);
    }

    private LoginRequest loginRequest() {
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