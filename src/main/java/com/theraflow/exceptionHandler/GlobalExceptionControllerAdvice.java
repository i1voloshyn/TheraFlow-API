package com.theraflow.exceptionHandler;

import com.theraflow.exception.PasswordPolicyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        URI path = URI.create(req.getRequestURI());
        String title = "Password does not meet the security requirements";
        int statusCode = HttpStatus.BAD_REQUEST.value();
        List<InvalidParam> invalidParams = ex.getViolations()
                .stream()
                .map(violation -> new InvalidParam(
                        violation.name().toLowerCase(Locale.ROOT),
                        violation.getMessageTemplate()
                ))
                .toList();
        ErrorResponse error = new ErrorResponse(
                title, path, statusCode, "Some message", invalidParams);
        return ResponseEntity
                .badRequest()
                .body(error);
    }

}
