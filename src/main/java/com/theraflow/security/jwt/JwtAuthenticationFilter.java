package com.theraflow.security.jwt;

import com.theraflow.security.AccountPrincipalService;
import com.theraflow.security.model.AccountPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;
    private final AccountPrincipalService accountPrincipalService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticateRequest(request);
        } catch (JwtException | AuthenticationException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            log.debug("Bearer-token authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String token = resolveBearerToken(request);
        if (token == null) {
            return;
        }

        String username = jwtService.extractUsernameFromAccessToken(token);
        AccountPrincipal accountPrincipal = accountPrincipalService.loadUserByUsername(username);

        if (!jwtService.validateAccessToken(token, accountPrincipal)) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        accountPrincipal,
                        null,
                        accountPrincipal.getAuthorities()
                );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private @Nullable String resolveBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
