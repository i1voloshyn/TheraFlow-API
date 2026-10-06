package com.theraflow.application.refreshToken;

import com.theraflow.authentication.model.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class UuidTokenProvider {

    private final Clock clock;

    @Value("${app.security.jwt.refresh.expiration}")
    private Duration refreshTokenExpiration;
    @Value("${app.security.password.reset.expiration}")
    private Duration passwordResetTokenExpiration;

    public RefreshToken buildRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        return new RefreshToken(tokenHash, getExpiration(refreshTokenExpiration));
    }

    public PasswordResetToken buildPasswordResetToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        return new PasswordResetToken(tokenHash, getExpiration(passwordResetTokenExpiration));
    }

    private Instant getExpiration(Duration expiredIn) {
        Instant now = clock.instant();
        return now.plus(expiredIn);
    }


    public String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Error hashing refresh tokens", e);
        }
    }

}
