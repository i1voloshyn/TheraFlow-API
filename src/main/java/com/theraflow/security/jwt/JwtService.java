package com.theraflow.security.jwt;

import com.theraflow.security.model.TheraflowUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${app.security.jwt.secret-key}")
    private String secretKey;
    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${app.security.jwt.refresh-token.expiration}")
    private long jwtRefreshExpiration;

    private Clock clock = Clock.systemUTC();

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Instant now = clock.instant();
        return extractExpiration(token).before(Date.from(now));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public TheraflowUser extractUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        UUID accountId = extractAccountId(claims);

        return new TheraflowUser(accountId, email, null);
    }

    private UUID extractAccountId(Claims claims) {
        String accountId = claims.get("accountId", String.class);

        return accountId!=null ? UUID.fromString(accountId):null;
    }

    public String generateToken(
            TheraflowUser userDetails
    ) {
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofMinutes(jwtExpiration));
        return Jwts
                .builder()
                .claim("accountId", userDetails.getAccountId().toString())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
}
