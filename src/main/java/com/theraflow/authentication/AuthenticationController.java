package com.theraflow.authentication;

import com.theraflow.application.refreshToken.UuidTokenProvider;
import com.theraflow.authentication.dto.JwtPair;
import com.theraflow.authentication.dto.LoginRequest;
import com.theraflow.authentication.dto.PasswordResetRequest;
import com.theraflow.authentication.dto.TokenRotateRequest;
import jakarta.validation.Valid;
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

    @PostMapping("/login")
    public ResponseEntity<JwtPair> login(
            @RequestBody LoginRequest request
    ) {
        JwtPair tokens = authenticationService.authenticate(request);

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtPair> refreshToken(
            @RequestBody TokenRotateRequest request
    ) {
        JwtPair tokens = authenticationService.rotateTokens(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        // Implement password reset logic here
        return ResponseEntity.ok().body("If the email is registered, you'll get a reset link");
    }
}
