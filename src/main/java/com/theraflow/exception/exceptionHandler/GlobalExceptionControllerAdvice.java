package com.theraflow.exception.exceptionHandler;

import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.PasswordPolicyException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.model.ErrorResponse;
import com.theraflow.exception.model.InvalidParam;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionControllerAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            HttpServletRequest req) {

        ErrorCode code = ErrorCode.RESOURCE_CONFLICT;
        ErrorResponse error = errorResponse(req, code, null, null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PasswordPolicyException.class)
    public ResponseEntity<ErrorResponse> handlePasswordPolicyException(
            PasswordPolicyException ex,
            HttpServletRequest req) {

        List<InvalidParam> invalidParams = ex.getViolations()
                .stream()
                .map(violation -> new InvalidParam(
                        violation.name().toLowerCase(Locale.ROOT),
                        violation.getMessageTemplate()
                ))
                .toList();
        ErrorResponse error = errorResponse(req, ErrorCode.PASSWORD_WEAK, null, invalidParams);

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(CurrentPasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrentPasswordMismatchException(
            HttpServletRequest req) {
        ErrorResponse error = errorResponse(req, ErrorCode.PASSWORD_INCORRECT, null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            HttpServletRequest req) {
        ErrorResponse error = errorResponse(req, ErrorCode.AUTHENTICATION_FAILED, null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest req) {

        ErrorResponse error = errorResponse(req, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        var errors = ex.getBindingResult().getAllErrors()
                .stream().map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return new InvalidParam(fieldName, errorMessage);
                }).toList();

        ErrorResponse error = errorResponse(req, ErrorCode.VALIDATION_FAILED, null, errors);

        return ResponseEntity.badRequest().body(error);
    }

    private ErrorResponse errorResponse(HttpServletRequest req,
                                        ErrorCode code,
                                        @Nullable String message,
                                        @Nullable List<InvalidParam> params
    ) {

        URI path = URI.create(req.getRequestURI());
        String errorMessage = code.getMessage()==null ? message:code.getMessage();

        return new ErrorResponse(code.getTitle(), path, code.getHttpStatus().value(), code, errorMessage, params);
    }

}
