package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.dto.AuthenticationResponse;
import com.theraflow.security.AuthenticationService;
import com.theraflow.security.model.LoginRequest;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody AccountRequest request
    ) {
        AccountResponse response = accountService.createAccount(request);
        String accessToken = authenticationService
                .authenticate(new LoginRequest(request.email(), request.rawPassword()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthenticationResponse(response, accessToken));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal TheraflowUser user
    ) {
        accountService.changePassword(request, user.getAccountId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> emailVerification(
            @RequestParam("token") @NotBlank String token
    ) {
        accountService.verifyEmail(token);

        return ResponseEntity.noContent().build();
    }
}
