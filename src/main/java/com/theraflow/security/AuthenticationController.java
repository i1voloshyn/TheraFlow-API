package com.theraflow.security;

import com.theraflow.security.model.LoginRequest;
import com.theraflow.security.model.Token;
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
    public ResponseEntity<Token> login(
            @RequestBody LoginRequest request
    ) {
        Token token = authenticationService.authenticate(request);

        return ResponseEntity.ok(token);
    }


}
