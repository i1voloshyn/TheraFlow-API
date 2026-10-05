package com.theraflow.application;

import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.model.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {
    private final Clock clock;

    public JwtBuilder tokenBuilder(
            Duration jwtExpiration,
            String secret
    ) {
        Instant now = clock.instant();
        Instant expiration = now.plus(jwtExpiration);
        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSignInKey(secret));
    }

    public Claims extractAllClaims(String token, String secret) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSignInKey(secret))
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("Token signature is valid, but the access has expired.", e);
            throw new PermissionException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token is completely invalid, structural failure, or tampered signature.", e);
            throw new InvalidCredentialsException(ErrorCode.TOKEN_INVALID);
        }
    }

    public <V> V extractClaim(String token, String secret, Function<Claims, V> claimResolver) {
        Claims claims = extractAllClaims(token, secret);
        return claimResolver.apply(claims);
    }

    private Key getSignInKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
