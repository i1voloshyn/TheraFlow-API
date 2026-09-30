package com.theraflow.security.exception;

import com.theraflow.exception.model.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AccessTokenException extends AuthenticationException {
    private final ErrorCode errorCode;

    public AccessTokenException(ErrorCode errorCode, Throwable e) {
        super(errorCode.getMessage(), e);
        this.errorCode = errorCode;
    }
}
