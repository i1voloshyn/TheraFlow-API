package com.theraflow.authentication;

import com.theraflow.account.AccountRepository;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.application.refreshToken.RefreshTokenService;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.exception.PermissionException;
import com.theraflow.exception.model.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtAuthTokenService jwtAuthTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @DisplayName("Should throw PermissionException for account with unverified email status ")
    @Test
    void authenticate_throwsException() {
        LoginRequest request = loginRequest();

      when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                        request.password()))).thenReturn(any());

        when(accountRepository.isEmailVerified(request.email())).thenReturn(false);

        assertThatExceptionOfType(PermissionException.class)
                .isThrownBy(() -> authenticationService.authenticate(request))
                .matches(ex -> ex.getErrorCode().equals(ErrorCode.EMAIL_NOT_VERIFIED));

        verifyNoInteractions(jwtAuthTokenService, refreshTokenService);
    }

    private LoginRequest loginRequest() {
        return new LoginRequest("valid-email", "valid-password");
    }

}