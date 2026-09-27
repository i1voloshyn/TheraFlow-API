package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.security.model.TheraflowUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {
    private final Clock clock = Clock.systemUTC();
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final JwtAuthTokenService jwtService;

    @Value("${app.security.jwt.refresh.expiration}")
    private Duration refreshTokenExpiration;

    public AuthTokenPair authenticate(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        TheraflowUser user = extractUser(auth);
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        return new AuthTokenPair(accessToken, rawRefreshToken);
    }

    public RefreshToken buildRefreshToken(String rawToken) {
        String tokenHash = hashRefreshToken(rawToken);

        Instant now = clock.instant();
        Instant expiration = now.plus(refreshTokenExpiration);

        return new RefreshToken(tokenHash,
                expiration);
    }

    String hashRefreshToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

    @Transactional
    public AuthTokenPair rotateTokens(String rawRefreshToken) {
        String tokenHash = hashRefreshToken(rawRefreshToken);
        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token")); //todo own exception

        if (oldRefreshToken.getExpiresAt().isBefore(clock.instant())) {
            throw new AuthenticationServiceException("Refresh token has expired");  //todo own exception
        }
//todo using revoked token should throw an exception and probably log the user out of all sessions
        if (oldRefreshToken.getIsRevoked()) {
            throw new AuthenticationServiceException("Refresh token is revoked"); //todo own exception
        }

        UUID accountId = Objects.requireNonNull(oldRefreshToken.getAccount().getId());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account", accountId)); //todo own

        TheraflowUser user = new TheraflowUser(account.getId(), account.getEmail(), null, account.getEmailVerified());

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRawRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = buildRefreshToken(newRawRefreshToken);

        oldRefreshToken.setIsRevoked(true);

        account.setRefreshToken(newRefreshToken);

        return new AuthTokenPair(newAccessToken, newRawRefreshToken);
    }

    private TheraflowUser extractUser(Authentication auth) {
        if (auth.getPrincipal() instanceof TheraflowUser user) {
            return user;
        } else {
            throw new AuthenticationServiceException("Unexpected authentication principal type");
        }
    }
}
