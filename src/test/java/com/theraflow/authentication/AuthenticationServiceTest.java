package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.testcontainers.shaded.com.trilead.ssh2.auth.AuthenticationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtAuthTokenService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @DisplayName("Should thrown an exception when refresh token is expired")
    @Test
    void rotateToken_sc1() {

    }

    @DisplayName("Should thrown an exception when refresh token is revoked")
    @Test
    void rotateToken_sc2() {
        String rawToken = "123e4567-e89b-12d3-a456-426614174000";
        String rawTokenHash = authenticationService.hashRefreshToken(rawToken);
        RefreshToken oldRefreshToken = new RefreshToken();
        oldRefreshToken.setTokenHash(rawTokenHash);
        oldRefreshToken.setIsRevoked(true);

        when(refreshTokenRepository.findByTokenHash(rawTokenHash)).thenReturn(Optional.of(oldRefreshToken));

        assertThatExceptionOfType(AuthenticationServiceException.class)
                .isThrownBy(() -> authenticationService.rotateTokens(rawToken))
                .withMessage("Refresh token is revoked");

        verifyNoInteractions(accountRepository, jwtService, authenticationManager);
    }

}