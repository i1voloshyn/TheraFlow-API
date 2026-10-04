package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshToken;
import com.theraflow.application.refreshToken.RefreshTokenService;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final Supplier<UUID> uuidSupplier;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthTokenService jwtAuthTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AccountRepository accountRepository;

    @Transactional
    public AuthTokenPair authenticate(LoginRequest request) {
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
        String accessToken = jwtAuthTokenService.generateAccessToken(user);
        String rawRefreshToken = uuidSupplier.get().toString();

        saveRefreshToken(user.getAccountId(), rawRefreshToken);

        return new AuthTokenPair(accessToken, rawRefreshToken);
    }

    private void saveRefreshToken(UUID accountId, String rawToken) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, accountId));
        RefreshToken token = refreshTokenService.buildRefreshToken(rawToken);
        account.setRefreshToken(token);
    }

    private TheraflowUser extractUser(Authentication auth) {
        if (auth.getPrincipal() instanceof TheraflowUser user) {
            return user;
        } else {
            throw new AuthenticationServiceException("Unexpected authentication principal type");
        }
    }
}
