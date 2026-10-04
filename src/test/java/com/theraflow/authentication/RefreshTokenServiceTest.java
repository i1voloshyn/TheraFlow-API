package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.application.refreshToken.RefreshTokenService;
import com.theraflow.exception.TheraflowApiException;
import com.theraflow.exception.model.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.trilead.ssh2.auth.AuthenticationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtAuthTokenService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @DisplayName("Should thrown an exception when refresh tokens is expired")
    @Test
    void rotateToken_sc1() {

    }

    @DisplayName("Should thrown an exception with REFRESH_TOKEN_REVOKED code when refresh tokens is revoked")
    @Test
    void rotateToken_sc2() {
        String rawToken = "123e4567-e89b-12d3-a456-426614174000";
        String rawTokenHash = refreshTokenService.hashRefreshToken(rawToken);
        RefreshToken oldRefreshToken = new RefreshToken();
        oldRefreshToken.setTokenHash(rawTokenHash);
        oldRefreshToken.setIsRevoked(true);

        when(refreshTokenRepository.findByTokenHash(rawTokenHash)).thenReturn(Optional.of(oldRefreshToken));

        assertThatExceptionOfType(TheraflowApiException.class)
                .isThrownBy(() -> refreshTokenService.rotateTokens(rawToken))
                .matches(ex -> ex.getErrorCode().name().equals(ErrorCode.REFRESH_TOKEN_REVOKED.name()));

        verifyNoInteractions(accountRepository, jwtService, authenticationManager);
    }

}