package com.theraflow.exception;

import com.theraflow.util.PasswordViolation;
import lombok.Getter;

import java.util.Set;

@Getter
public class PasswordPolicyException extends RuntimeException {
    private final Set<PasswordViolation> violations;

    public PasswordPolicyException(Set<PasswordViolation> violations) {
        super("Password does not satisfied the rawPassword policy");
        this.violations = Set.copyOf(violations);
    }
}
