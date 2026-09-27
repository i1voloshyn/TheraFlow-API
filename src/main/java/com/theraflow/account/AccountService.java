package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.SignUpResponse;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtEmailVerificationTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.authentication.AuthenticationService;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.event.VerificationEmailRequested;
import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import com.theraflow.util.PasswordViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {
    private static final String ACCOUNT = "Account";

    private final AuthenticationService authenticationService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final DtoAccountMapper mapper;
    private final JwtEmailVerificationTokenService emailVerificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SignUpResponse signUp(AccountRequest request) {
        passwordValidator.validate(request.rawPassword());
        Account account = save(request);

        AuthTokenPair tokens = authenticationService.authenticate(
                new LoginRequest(request.email(), request.rawPassword()));
        RefreshToken refreshToken = authenticationService.buildRefreshToken(tokens.refresh());
        account.setRefreshToken(refreshToken);

        //todo Uncomment it later. Keep it commented just for postman testing for not sending emails
      //  publishSentEmailEvent(account);

        return new SignUpResponse(mapper.toResponse(account), tokens);
    }

    private Account save(AccountRequest request) {
        Account account = mapper.toAccount(
                request.email(),
                passwordEncoder.encode(request.rawPassword()),
                request.type()
        );
        return accountRepository.save(account);
    }

    private void publishSentEmailEvent(Account account) {
        String verificationToken = emailVerificationService.generateToken(account.getEmail());
        eventPublisher.publishEvent(new VerificationEmailRequested(
                account.getId(),
                account.getEmail(),
                verificationToken
        ));
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
        String email = emailVerificationService.extractEmail(token);

        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, email));

        account.setEmailVerified(true);
    }
}
