package com.theraflow.service;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.event.VerificationEmailRequested;
import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.TokenExpiredException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.AuthService;
import com.theraflow.security.jwt.JWTService;
import com.theraflow.security.model.LoginRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AccountService {
    private static final String ACCOUNT = "Account";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final DtoAccountMapper mapper;
    private final AuthService authService;
    private final JWTService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        passwordValidator.validate(request.rawPassword());
        String verificationToken = jwtService.generateEmailVerificationToken(request.email());

        Account accountToSave = mapper.toAccount(
                request.email(),
                passwordEncoder.encode(request.rawPassword()),
                request.type(),
                verificationToken
        );
        Account createdAccount = accountRepository.saveAndFlush(accountToSave);
        eventPublisher.publishEvent(new VerificationEmailRequested(
                createdAccount.getId(),
                createdAccount.getEmail(),
                verificationToken
        ));

        String token = authService.authenticate(new LoginRequest(request.email(), request.rawPassword()));

        return mapper.toResponse(createdAccount, token);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, id));
        if (!passwordEncoder.matches(request.oldPassword(), account.getPasswordHash())) {
            throw new CurrentPasswordMismatchException();
        }

        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new PasswordPolicyException(Set.of(PasswordViolation.SAME_AS_CURRENT));
        }

        passwordValidator.validate(request.newPassword());
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void verifyEmail(String token) {
        final String email;
        try {
            email = jwtService.extractEmailFromVerificationToken(token);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("The email verification link has expired. Please request a new one.");
        }

        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, email));

        account.setVerificationToken(null);
        account.setVerified(true);
    }
}
