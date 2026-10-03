package com.theraflow.authentication;

import com.theraflow.application.refreshToken.RefreshTokenService;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.TokenRotateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenPair> login(
            @RequestBody LoginRequest request
    ) {
        AuthTokenPair tokens = authenticationService.authenticate(request);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenPair> refreshToken(
            @RequestBody TokenRotateRequest request
    ) {
        AuthTokenPair tokens = refreshTokenService.rotateTokens(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }
}
