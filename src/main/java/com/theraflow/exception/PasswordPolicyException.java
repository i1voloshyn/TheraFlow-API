package com.theraflow.exception;

import com.theraflow.exception.model.ErrorCode;
import com.theraflow.util.PasswordViolation;
import lombok.Getter;

import java.util.Set;

@Getter
public class PasswordPolicyException extends TheraflowApiException {
    private final Set<PasswordViolation> violations;

    public PasswordPolicyException(ErrorCode errorCode, Set<PasswordViolation> violations) {
        super(errorCode);
        this.violations = Set.copyOf(violations);
    }
}
