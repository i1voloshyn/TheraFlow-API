package com.theraflow.util;

import lombok.Getter;

@Getter
public enum PasswordViolation {
    TOO_SHORT("Password must be at least 10 characters long"),
    TOO_LONG("Password cannot exceed 64 characters."),
    MISSING_UPPERCASE_LETTER("Password must contain at least one uppercase letter."),
    MISSING_SPECIAL_CHARACTER("Password must contain at least one special character (e.g., !@#$%^&*)."),
    MISSING_NUMBER("Password must contain at least one number.");

    private final String messageTemplate;

    PasswordViolation(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }
}
