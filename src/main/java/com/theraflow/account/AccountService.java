package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtEmailVerificationTokenProvider;
import com.theraflow.event.VerificationEmailRequested;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.ResourceConflictException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final DtoAccountMapper mapper;
    private final JwtEmailVerificationTokenProvider jwtEmailVerificationTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse signUp(AccountRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        passwordValidator.validate(request.rawPassword());
        Account toSave = mapper.toAccount(
                request.email(),
                passwordEncoder.encode(request.rawPassword()),
                request.type()
        );
        Account saved =  accountRepository.save(toSave);
        publishSentEmailEvent(toSave);

        return mapper.toResponse(saved);
    }

    private void publishSentEmailEvent(Account account) {
        String verificationToken = jwtEmailVerificationTokenProvider.generateToken(account.getEmail());
        eventPublisher.publishEvent(new VerificationEmailRequested(
                account.getId(),
                account.getEmail(),
                verificationToken
        ));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UUID id) {
        passwordValidator.validate(request.newPassword());

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, id));
        if (!passwordEncoder.matches(request.oldPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorCode.PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResourceConflictException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void verifyEmail(String token) {
        final String email;
        try {
            email = jwtEmailVerificationTokenProvider.extractEmail(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException(ErrorCode.EMAIL_VERIFICATION_LINK_EXPIRED);
        }

        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, email));

        account.setEmailVerified(true);

    }
}
