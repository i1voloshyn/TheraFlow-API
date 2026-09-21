package com.theraflow.security.model;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TheraflowUser implements UserDetails {

    public static final String VERIFIED_AUTHORITY = "ACCOUNT_VERIFIED";

    @Getter
    private final UUID accountId;
    private final String email;
    @Nullable
    private final String password;
    @Getter
    private final boolean verified;

    public TheraflowUser(UUID accountId, String email, @Nullable String password, boolean verified) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.verified = verified;
    }

    public TheraflowUser(UUID accountId, String email, @Nullable String password) {
        this(accountId, email, password, false);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return verified
                ? List.of(new SimpleGrantedAuthority(VERIFIED_AUTHORITY))
                : List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
