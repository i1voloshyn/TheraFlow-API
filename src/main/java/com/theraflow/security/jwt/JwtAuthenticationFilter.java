package com.theraflow.security.jwt;

import com.theraflow.security.AccountPrincipalService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService JWTService;
    private final AccountPrincipalService accountPrincipalService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader!=null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = JWTService.extractUserName(token);
        }

        if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails accountPrincipal = accountPrincipalService.loadUserByUsername(username);
            if (JWTService.validateToken(token, accountPrincipal)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(accountPrincipal, null, accountPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
