package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.model.Account;
import com.theraflow.event.VerificationEmailRequested;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.TokenExpiredException;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import com.theraflow.util.PasswordValidator;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService  {
    private static final String ACCOUNT = "Account";

    private final AccountRepository accountRepository;
    //  private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final DtoAccountMapper mapper;
    //    private final AuthService authService;
//    private final JWTService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        passwordValidator.validate(request.rawPassword());
        //   String verificationToken = jwtService.generateEmailVerificationToken(request.email());
        String verificationToken = "some token";

        Account accountToSave = mapper.toAccount(
                request.email(),
                //  passwordEncoder.encode(request.rawPassword()),
                "",
                request.type(),
                verificationToken
        );
        accountRepository.save(accountToSave);
        eventPublisher.publishEvent(new VerificationEmailRequested(
                accountToSave.getId(),
                accountToSave.getEmail(),
                verificationToken
        ));

        //   String token = authService.authenticate(new LoginRequest(request.email(), request.rawPassword()));
        String token = "";

        return mapper.toResponse(accountToSave, token);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, id));
//        if (!passwordEncoder.matches(request.oldPassword(), account.getPasswordHash())) {
//            throw new CurrentPasswordMismatchException();
//        }
//
//        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
//            throw new PasswordPolicyException(Set.of(PasswordViolation.SAME_AS_CURRENT));
//        }

        passwordValidator.validate(request.newPassword());
        // account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void verifyEmail(String token) {
        final String email;
        try {
            //  email = jwtService.extractEmailFromVerificationToken(token);
            email = "";
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("The email verification link has expired. Please request a new one.");
        }

        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ACCOUNT, email));

        account.setVerificationToken(null);
        account.setVerified(true);
    }
}
