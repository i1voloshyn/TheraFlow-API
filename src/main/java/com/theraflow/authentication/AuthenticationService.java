package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtAuthTokenProvider;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenRepository;
import com.theraflow.application.refreshToken.UuidTokenProvider;
import com.theraflow.authentication.dto.ConfirmPasswordResetRequest;
import com.theraflow.authentication.dto.JwtPair;
import com.theraflow.authentication.dto.LoginRequest;
import com.theraflow.authentication.model.PasswordResetToken;
import com.theraflow.event.PasswordResetRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.TheraflowApiException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final Supplier<UUID> uuidSupplier;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthTokenProvider jwtAuthTokenProvider;
    private final UuidTokenProvider uuidTokenProvider;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Transactional
    public JwtPair authenticate(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        if (!accountRepository.isEmailVerified(request.email())) {
            throw new PermissionException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        TheraflowUser user = extractUser(auth);
        String accessToken = jwtAuthTokenProvider.generateAccessToken(user);
        String rawRefreshToken = uuidSupplier.get().toString();

        saveRefreshToken(user.getAccountId(), rawRefreshToken);

        return new JwtPair(accessToken, rawRefreshToken);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        accountRepository.findAccountByEmail(email).ifPresent(account -> {
            String rawResetToken = uuidSupplier.get().toString();

            PasswordResetToken resetToken = uuidTokenProvider.buildPasswordResetToken(rawResetToken);
            account.setPasswordResetToken(resetToken);

            eventPublisher.publishEvent(new PasswordResetRequest(
                    email,
                    rawResetToken
            ));
        });
    }

    @Transactional(readOnly = true)
    public void verifyPasswordReset(String token) {
        findValidPasswordResetToken(token);
    }

    @Transactional
    public void confirmPasswordReset(ConfirmPasswordResetRequest request) {
        passwordValidator.validate(request.newPassword());

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PermissionException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        PasswordResetToken resetToken = findValidPasswordResetToken(request.token());
        Account account = resetToken.getAccount();

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsed(true);
    }

    // todo: Add tests
    @Transactional
    public JwtPair rotateTokens(String rawRefreshToken) {
        String tokenHash = uuidTokenProvider.hashToken(rawRefreshToken);
        RefreshToken oldRefreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh tokens")); //todo own exception

        if (oldRefreshToken.getIsRevoked()) {
            throw new TheraflowApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (oldRefreshToken.getExpiresAt().isBefore(clock.instant())) {
            throw new PermissionException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        UUID accountId = Objects.requireNonNull(oldRefreshToken.getAccount().getId());
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, accountId));

        JwtPair tokens = generateTokenPair(account);
        RefreshToken newRefreshToken = uuidTokenProvider.buildRefreshToken(tokens.refresh());

        oldRefreshToken.setIsRevoked(true);
        account.setRefreshToken(newRefreshToken);

        return new JwtPair(tokens.access(), tokens.refresh());
    }

    private JwtPair generateTokenPair(Account account) {
        TheraflowUser user = new TheraflowUser(
                account.getId(),
                account.getEmail(),
                null,
                account.getEmailVerified()
                , account.getType());

        String accessToken = jwtAuthTokenProvider.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        return new JwtPair(accessToken, rawRefreshToken);
    }

    private void saveRefreshToken(UUID accountId, String rawToken) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, accountId));
        RefreshToken token = uuidTokenProvider.buildRefreshToken(rawToken);
        account.setRefreshToken(token);
    }

    private PasswordResetToken findValidPasswordResetToken(String rawToken) {
        String tokenHash = uuidTokenProvider.hashToken(rawToken);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new PermissionException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(clock.instant())) {
            throw new PermissionException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        return resetToken;
    }

    private TheraflowUser extractUser(Authentication auth) {
        if (auth.getPrincipal() instanceof TheraflowUser user) {
            return user;
        } else {
            throw new AuthenticationServiceException("Unexpected authentication principal type");
        }
    }
}
