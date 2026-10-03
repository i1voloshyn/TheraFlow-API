package com.theraflow.application.refreshToken;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.TheraflowApiException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final Clock clock;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final JwtAuthTokenService jwtService;

    @Value("${app.security.jwt.refresh.expiration}")
    private Duration refreshTokenExpiration;

    public RefreshToken buildRefreshToken(String rawToken) {
        String tokenHash = hashRefreshToken(rawToken);

        Instant now = clock.instant();
        Instant expiration = now.plus(refreshTokenExpiration);

        return new RefreshToken(tokenHash,
                expiration);
    }

    public String hashRefreshToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

    // todo: Add tests
    @Transactional
    public AuthTokenPair rotateTokens(String rawRefreshToken) {
        String tokenHash = hashRefreshToken(rawRefreshToken);
        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token")); //todo own exception

        if (oldRefreshToken.getIsRevoked()) {
            throw new TheraflowApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (oldRefreshToken.getExpiresAt().isBefore(clock.instant())) {
            throw new AuthenticationServiceException("Refresh token has expired");  //todo own exception
        }

        UUID accountId = Objects.requireNonNull(oldRefreshToken.getAccount().getId());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, accountId));

        AuthTokenPair tokens = generateTokenPair(account);
        RefreshToken newRefreshToken = buildRefreshToken(tokens.refresh());

        oldRefreshToken.setIsRevoked(true);
        account.setRefreshToken(newRefreshToken);

        return new AuthTokenPair(tokens.access(), tokens.refresh());
    }

    public AuthTokenPair generateTokenPair(Account account) {
        TheraflowUser user = new TheraflowUser(
                account.getId(),
                account.getEmail(),
                null,
                account.getEmailVerified()
                , account.getType());

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        return new AuthTokenPair(accessToken, rawRefreshToken);
    }

}
