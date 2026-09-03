package com.theraflow.service;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.TokenExpiredException;
import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.repository.AccountRepository;
import com.theraflow.security.AuthService;
import com.theraflow.security.jwt.JWTService;
import com.theraflow.security.model.LoginRequest;
import com.theraflow.util.PasswordValidator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    private final EmailService emailService;

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
        emailService.sendVerificationEmail(request.email(), verificationToken);

        String token = authService.authenticate(new LoginRequest(request.email(), request.rawPassword()));

        return mapper.toResponse(createdAccount, token);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, id));

        if (!passwordEncoder.matches(request.oldPassword(), account.getPasswordHash())) {
            throw new BadCredentialsException("The old password does not match your current password");
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void verifyEmail(String token) {
        if (jwtService.isEmailVerificationTokenExpired(token)) {
            throw new TokenExpiredException("The email verification link has expired. Please request a new one.");
        }
        String email = jwtService.extractUserName(token);
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, email));

        account.setVerificationToken(null);
        account.setVerified(true);
    }
}
