package com.theraflow.util;

import com.theraflow.config.PasswordLengthProperties;
import com.theraflow.exception.PasswordPolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PasswordValidator {

    private final PasswordLengthProperties passwordLengthProperties;

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
        if (password.length() < passwordLengthProperties.minLength()) {
            violations.add(PasswordViolation.TOO_SHORT);
        }
        if (password.length() > passwordLengthProperties.maxLength()) {
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
