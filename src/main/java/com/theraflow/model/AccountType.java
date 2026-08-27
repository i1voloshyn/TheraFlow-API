package com.theraflow.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum AccountType {
    GUARDIAN, THERAPIST;

    public @NotNull SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
