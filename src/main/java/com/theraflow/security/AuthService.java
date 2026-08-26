package com.theraflow.security;

import com.theraflow.security.jwt.JWTService;
import com.theraflow.security.model.AccountPrincipal;
import com.theraflow.security.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JWTService JWTService;

    public String authenticate(LoginRequest request) {
        Authentication authentication =
                authManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password())
                );
        AccountPrincipal principal = extractAccountPrincipal(authentication);

        return JWTService.generateToken(principal);
    }

    private AccountPrincipal extractAccountPrincipal(Authentication auth) {
        if (auth.getPrincipal() instanceof AccountPrincipal accountPrincipal) {
            return accountPrincipal;
        } else {
            throw new AuthenticationServiceException("Unexpected authenticated principal type");
        }
    }
}
