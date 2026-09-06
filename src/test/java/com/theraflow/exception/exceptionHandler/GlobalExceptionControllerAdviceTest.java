package com.theraflow.exception.exceptionHandler;

import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionControllerAdviceTest {
    private final GlobalExceptionControllerAdvice advice =
            new GlobalExceptionControllerAdvice();

    @Test
    void badLoginCredentials_returnGenericAuthenticationError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        ErrorResponse error = advice.handleBadCredentialsException(
                new BadCredentialsException("Internal authentication details"),
                request
        ).getBody();

        assertThat(error).isNotNull();
        assertThat(error.title()).isEqualTo("Authentication failed");
        assertThat(error.errorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        assertThat(error.message()).isEqualTo("Invalid email or password");
    }

    @Test
    void incorrectOldPassword_returnsPasswordMismatchError() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("PATCH", "/api/v1/accounts/change-password");

        ErrorResponse error = advice.handleCurrentPasswordMismatchException(
                new CurrentPasswordMismatchException(),
                request
        ).getBody();

        assertThat(error).isNotNull();
        assertThat(error.title()).isEqualTo("Incorrect old password");
        assertThat(error.errorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
    }
}
