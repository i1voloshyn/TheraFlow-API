package com.theraflow.jwt;

import com.theraflow.exception.JwtValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.function.Function;

@Slf4j
public abstract class AbstractJwtService<T> {
    protected final Clock clock = Clock.systemUTC();

    protected abstract String getSecretKey();

    public abstract String generateToken(T subject);

    protected Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    protected final Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSignInKey())
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.info("Token signature is valid, but the token has expired.");
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token is completely invalid, structural failure, or tampered signature.");
            throw new JwtValidationException("Invalid token status", e);
        }
    }

    protected final <V> V extractClaim(String token, Function<Claims, V> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
}
