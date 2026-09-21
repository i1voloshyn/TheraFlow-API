package com.theraflow.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.AuthenticationException;

public class JwtExpiredException extends AuthenticationException {
    public JwtExpiredException(@Nullable String msg, Throwable cause) {
        super(msg, cause);
    }
}
