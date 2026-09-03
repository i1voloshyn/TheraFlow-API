package com.theraflow.security.jwt;

import com.theraflow.security.model.AccountPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JWTService {
    private final Key secretKey;
    private final Clock clock;

    public String generateAuthToken(AccountPrincipal account) {
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofHours(10L));
        return Jwts.builder()
                .subject(account.getUsername())
                .claim("role", account.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority).toList())
                .claim("accountID", account.getAccountId())
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
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    //Validate by ID probably be better
    public boolean validateToken(String token, UserDetails accountPrincipal) {
        String userName = extractUserName(token);
        return (userName.equals(accountPrincipal.getUsername()) && !isTokenExpired(token));
    }

    public boolean isEmailVerificationTokenExpired(String token) {
        return isTokenExpired(token);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(now()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Instant now() {
        return clock.instant();
    }
}
