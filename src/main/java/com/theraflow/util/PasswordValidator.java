package com.theraflow.util;

import com.theraflow.exception.PasswordPolicyException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PasswordValidator {
    private static final int MIN_PASSWORD_LENGTH = 10;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final Set<PasswordViolation> violations = new HashSet<>();

    public void validate(String password) {
        checkPasswordLength(password);

        if (!containsSpecialCharacter(password)) {
            violations.add(PasswordViolation.MISSING_SPECIAL_CHARACTER);
        }

        if (!containsUppercaseLetter(password)) {
            violations.add(PasswordViolation.MISSING_UPPERCASE_LETTER);
        }

        if (!containsDigit(password)) {
            violations.add(PasswordViolation.MISSING_NUMBER);
        }

        if (!violations.isEmpty()) {
            throw new PasswordPolicyException(violations);
        }
    }

    private void checkPasswordLength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            violations.add(PasswordViolation.TOO_SHORT);
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            violations.add(PasswordViolation.TOO_LONG);
        }
    }

    private boolean containsSpecialCharacter(String password) {
        return password.codePoints()
                .anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
    }

    private boolean containsUppercaseLetter(String password) {
        return password.codePoints()
                .anyMatch(Character::isUpperCase);
    }

    private boolean containsDigit(String password) {
        return password.codePoints()
                .anyMatch(Character::isDigit);
    }
}
