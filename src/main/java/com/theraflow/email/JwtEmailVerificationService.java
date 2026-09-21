package com.theraflow.email;

import com.theraflow.jwt.AbstractJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtEmailVerificationService extends AbstractJwtService<String> {

    @Value("${app.security.jwt.email.secret-key}")
    private String secretKey;
    @Value("${app.security.jwt.email.expiration}")
    private long jwtExpiration;

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    protected String getSecretKey() {
        return secretKey;
    }

    @Override
    public String generateToken(String subject) {
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofMinutes(jwtExpiration));
        return Jwts
                .builder()
                .subject(subject) // Email provided by user
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSignInKey())
                .compact();
    }
}
