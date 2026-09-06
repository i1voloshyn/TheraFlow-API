package com.theraflow.security.jwt;

import com.theraflow.security.model.AccountPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class JWTService {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCOUNT_ID_CLAIM = "accountId";

    private final Key secretKey;
    private final Clock clock;

    public String generateAuthToken(AccountPrincipal account) {
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofHours(10L));
        return Jwts.builder()
                .subject(account.getUsername())
                .claim("role", account.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority).toList())
                .claim(ACCOUNT_ID_CLAIM, account.getAccountId().toString())
                .claim(TOKEN_TYPE_CLAIM, TokenType.ACCESS.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateEmailVerificationToken(String email) {
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofHours(1L));
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, TokenType.EMAIL_VERIFICATION.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateAccessToken(String token, AccountPrincipal accountPrincipal) {
        Claims claims = extractAllClaims(token);

        return hasTokenType(claims, TokenType.ACCESS)
                && accountPrincipal.getUsername().equals(claims.getSubject())
                && accountPrincipal.getAccountId().toString()
                .equals(claims.get(ACCOUNT_ID_CLAIM, String.class));
    }

    public String extractUsernameFromAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        requireTokenType(claims, TokenType.ACCESS);
        return claims.getSubject();
    }

    public String extractEmailFromVerificationToken(String token) {
        Claims claims = extractAllClaims(token);
        requireTokenType(claims, TokenType.EMAIL_VERIFICATION);
        return claims.getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean hasTokenType(Claims claims, TokenType expectedType) {
        return expectedType.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    private void requireTokenType(Claims claims, TokenType expectedType) {
        if (!hasTokenType(claims, expectedType)) {
            throw new JwtException("Expected a " + expectedType + " token");
        }
    }

    private Instant now() {
        return clock.instant();
    }

    private enum TokenType {
        ACCESS,
        EMAIL_VERIFICATION
    }
}
