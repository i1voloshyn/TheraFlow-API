package com.theraflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "theraflow.password-length")
public record PasswordLengthProperties(
        int minLength,
        int maxLength
) {
}
