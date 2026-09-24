package com.theraflow.application;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class JwtEmailVerificationTokenService {

    private final JwtService jwtService;

    @Value("${app.security.jwt.email.secret}")
    private String secret;
    @Value("${app.security.jwt.email.expiration}")
    private Duration expiration;

    public String extractEmail(String token) {
        return jwtService.extractClaim(token, secret, Claims::getSubject);
    }

    public String generateToken(String subject) {
        return jwtService
                .buildToken(expiration, secret)
                .subject(subject)
                .compact();
    }
}
