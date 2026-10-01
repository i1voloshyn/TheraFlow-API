package com.theraflow.application;

import com.theraflow.account.model.AccountType;
import com.theraflow.security.model.TheraflowUser;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthTokenService {
    private static final String VERIFIED_CLAIM = "verified";
    private static final String ACCOUNT_TYPE_CLAIM = "account_type";

    private final JwtService jwtService;

    @Value("${app.security.jwt.access.secret}")
    private String accessSecret;
    @Value("${app.security.jwt.access.expiration}")
    private Duration accessExpiration;

    public TheraflowUser extractUserDetails(String token) {
        Claims claims = jwtService.extractAllClaims(token, accessSecret);
        String email = claims.getSubject();
        UUID accountId = extractAccountId(claims);
        boolean verified = Boolean.TRUE.equals(claims.get(VERIFIED_CLAIM, Boolean.class));
        AccountType type = claims.get(ACCOUNT_TYPE_CLAIM, AccountType.class);

        return new TheraflowUser(accountId, email, null, verified, type);
    }

    private UUID extractAccountId(Claims claims) {
        String accountId = claims.get("accountId", String.class);
        return accountId!=null ? UUID.fromString(claims.get("accountId", String.class)):null;
    }

    public String generateAccessToken(
            TheraflowUser userDetails
    ) {
        return jwtService.tokenBuilder(accessExpiration, accessSecret)
                .claim("accountId", userDetails.getAccountId().toString())
                .claim(ACCOUNT_TYPE_CLAIM, userDetails.getType().name())
                .claim(VERIFIED_CLAIM, userDetails.isVerified())
                .subject(userDetails.getUsername())
                .compact();
    }
}
