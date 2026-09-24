package com.theraflow.application;

import com.theraflow.security.model.TheraflowUser;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class JwtAuthTokenService {
    private static final String VERIFIED_CLAIM = "verified";

    private final JwtService jwtService;

    public JwtAuthTokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Value("${app.security.jwt.access.secret}")
    private String accessSecret;
    @Value("${app.security.jwt.refresh.secret}")
    private String refreshSecret;
    @Value("${app.security.jwt.access.expiration}")
    private Duration accessExpiration;
    @Value("${app.security.jwt.refresh.expiration}")
    private Duration refreshExpiration;

    public TheraflowUser extractUserDetails(String token) {
        Claims claims = jwtService.extractAllClaims(token, accessSecret);
        String email = claims.getSubject();
        UUID accountId = extractAccountId(claims);
        boolean verified = Boolean.TRUE.equals(claims.get(VERIFIED_CLAIM, Boolean.class));

        return new TheraflowUser(accountId, email, null, verified);
    }

    private UUID extractAccountId(Claims claims) {
        String accountId = claims.get("accountId", String.class);

        return accountId!=null ? UUID.fromString(accountId):null;
    }

    public String generateAccessToken(
            TheraflowUser userDetails
    ) {
        return jwtService.buildToken(accessExpiration, accessSecret)
                .claim("accountId", userDetails.getAccountId().toString())
                .claim(VERIFIED_CLAIM, userDetails.isVerified())
                .subject(userDetails.getUsername())
                .compact();
    }

    public String generateRefreshToken(
            TheraflowUser userDetails
    ) {
        return jwtService.buildToken(refreshExpiration, refreshSecret)
                .claim("accountId", userDetails.getAccountId().toString())
                .compact();

    }

}
