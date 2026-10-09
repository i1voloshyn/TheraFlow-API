package com.theraflow.authentication;

import com.theraflow.authentication.dto.AccountRequest;
import com.theraflow.authentication.dto.AccountResponse;
import com.theraflow.authentication.dto.ConfirmPasswordResetRequest;
import com.theraflow.authentication.dto.JwtPair;
import com.theraflow.authentication.dto.LoginRequest;
import com.theraflow.authentication.dto.PasswordResetRequest;
import com.theraflow.authentication.dto.TokenRotateRequest;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public ResponseEntity<AccountResponse> signUp(
            @Valid @RequestBody AccountRequest request
    ) {
        AccountResponse response = authenticationService.signUp(request); //there is any sense to send an Account if user has to confirm email first

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtPair> login(
            @RequestBody LoginRequest request
    ) {
        JwtPair tokens = authenticationService.authenticate(request);

        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> emailVerification(
            @RequestParam("token") @NotBlank String token
    ) {
        authenticationService.verifyEmail(token);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtPair> rotateTokens(
            @RequestBody TokenRotateRequest request
    ) {
        JwtPair tokens = authenticationService.rotateTokens(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authenticationService.requestPasswordReset(request.email());
        return ResponseEntity.ok().body("If the email is registered, you'll get a reset link");
    }

    @GetMapping("/password-reset")
    public ResponseEntity<Void> verifyPasswordResetToken(
            @RequestParam("token") String token
    ) {
        authenticationService.verifyPasswordReset(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(
            @Valid @RequestBody ConfirmPasswordResetRequest request
    ) {
        authenticationService.confirmPasswordReset(request);
        return ResponseEntity.ok().body("Password reset successful");
    }
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        authenticationService.changePassword(request, user.getAccountId());

        return ResponseEntity.noContent().build();
    }
}
