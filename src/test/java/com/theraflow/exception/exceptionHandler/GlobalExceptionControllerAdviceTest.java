package com.theraflow.exception.exceptionHandler;

import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionControllerAdviceTest {
    private final GlobalExceptionControllerAdvice advice =
            new GlobalExceptionControllerAdvice();

    @Test
    void badLoginCredentials_returnGenericAuthenticationError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        ErrorResponse error = advice.handleBadCredentialsException(
                request
        ).getBody();

        assertThat(error).isNotNull();
        assertThat(error.title()).isEqualTo("Authentication failed");
        assertThat(error.errorCode()).isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
    }
}
