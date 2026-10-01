package com.theraflow.security.model;

import com.theraflow.account.model.AccountType;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TheraflowUser implements UserDetails {

    @Getter
    private final UUID accountId;
    private final String email;
    @Nullable
    private final String password;
    @Getter
    private final boolean verified;
    @Getter
    private final AccountType type;

    public TheraflowUser(UUID accountId, String email, @Nullable String password, boolean verified, AccountType type) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.verified = verified;
        this.type = type;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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
