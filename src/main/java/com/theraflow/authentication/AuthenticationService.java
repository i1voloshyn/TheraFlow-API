package com.theraflow.authentication;

import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.security.model.TheraflowUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final Clock clock = Clock.systemUTC();
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtAuthTokenService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.jwt.refresh.expiration}")
    private Duration jwtExpiration;

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

    public RefreshToken buildRefreshToken(String token) {
        String tokenHash = hashRefreshToken(token);

        Instant now = clock.instant();
        Instant expiration = now.plus(jwtExpiration);

        return new RefreshToken(tokenHash,
                expiration);
    }

    private String hashRefreshToken(String token) {
        return passwordEncoder.encode(token);
    }

    public AuthTokenPair refreshTokens(String refreshToken) {

    }


    private TheraflowUser extractUser(Authentication auth) {
        if (auth.getPrincipal() instanceof TheraflowUser user) {
            return user;
        } else {
            throw new AuthenticationServiceException("Unexpected authentication principal type");
        }
    }
}
