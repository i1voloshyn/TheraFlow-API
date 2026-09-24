package com.theraflow.security.jwt;

import com.theraflow.jwt.AbstractJwtService;
import com.theraflow.security.model.TheraflowUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtAuthenticationService extends AbstractJwtService<TheraflowUser> {
    private static final String VERIFIED_CLAIM = "verified";

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;
    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${app.security.jwt.refresh-token.expiration}")
    private long jwtRefreshExpiration;

    public TheraflowUser extractUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        UUID accountId = extractAccountId(claims);
        boolean verified = Boolean.TRUE.equals(claims.get(VERIFIED_CLAIM, Boolean.class));

        return new TheraflowUser(accountId, email, null, verified);
    }

    private UUID extractAccountId(Claims claims) {
        String accountId = claims.get("accountId", String.class);

        return accountId!=null ? UUID.fromString(accountId):null;
    }

    @Override
    protected String getSecretKey() {
        return secretKey;
    }

    public String generateAccessToken(
            TheraflowUser userDetails
    ) {
        return buildToken(userDetails, jwtExpiration);
    }

    public String generateRefreshToken(
            TheraflowUser userDetails
    ) {
        return buildToken(userDetails, jwtRefreshExpiration);
    }

    private String buildToken(TheraflowUser userDetails, long jwtExpiration){
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofMinutes(jwtExpiration));
        return Jwts
                .builder()
                .claim("accountId", userDetails.getAccountId().toString())
                .claim(VERIFIED_CLAIM, userDetails.isVerified())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSignInKey())
                .compact();
    }
}
