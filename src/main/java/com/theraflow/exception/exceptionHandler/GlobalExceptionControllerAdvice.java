package com.theraflow.exception.exceptionHandler;

import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.model.ErrorResponse;
import com.theraflow.exception.model.InvalidParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionControllerAdvice {

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<ErrorResponse> handlePasswordPolicyException(
            PasswordPolicyException ex,
            HttpServletRequest req) {
        String title = "Password does not meet the security requirements";
        URI path = URI.create(req.getRequestURI());
        int statusCode = HttpStatus.BAD_REQUEST.value();
        var errorCode = ErrorCode.WEAK_PASSWORD;
        List<InvalidParam> invalidParams = ex.getViolations()
                .stream()
                .map(violation -> new InvalidParam(
                        violation.name().toLowerCase(Locale.ROOT),
                        violation.getMessageTemplate()
                ))
                .toList();
        ErrorResponse error = new ErrorResponse(
                title, path, statusCode, errorCode, ex.getMessage(), invalidParams);
        return ResponseEntity
                .badRequest()
                .body(error);

    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest req) {
        String title = "Incorrect old password";
        URI path = URI.create(req.getRequestURI());
        int statusCode = HttpStatus.UNAUTHORIZED.value();
        var errorCode = ErrorCode.PASSWORD_MISMATCH;
        ErrorResponse error = new ErrorResponse(title, path, statusCode, errorCode, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
