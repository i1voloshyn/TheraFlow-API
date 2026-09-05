package com.theraflow.util;

import com.theraflow.config.PasswordLengthProperties;
import com.theraflow.exception.PasswordPolicyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValidatorTest {
    private final PasswordValidator validator =
            new PasswordValidator(new PasswordLengthProperties(10, 64));

    @Test
    void validate_afterInvalidPassword_doesNotKeepPreviousViolations() {
        assertThatThrownBy(() -> validator.validate("weak"))
                .isInstanceOf(PasswordPolicyException.class);

        assertThatCode(() -> validator.validate("StrongPassword1!"))
                .doesNotThrowAnyException();
    }
}
