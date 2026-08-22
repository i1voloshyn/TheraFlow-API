package com.theraflow.controller;

import com.theraflow.service.AccountService;
import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> register(
            @Valid @RequestBody AccountRequest request
    ) {
        AccountResponse response = accountService.createAccount(request);
        URI location = URI.create("/api/v1/accounts/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }
}
