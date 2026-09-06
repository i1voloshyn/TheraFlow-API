package com.theraflow.security.model;

import com.theraflow.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class AccountPrincipal implements UserDetails {
    private static final GrantedAuthority UNVERIFIED_AUTHORITY =
            new SimpleGrantedAuthority("ROLE_UNVERIFIED");

    @Getter
    private UUID accountId;
    private String email;
    private String passwordHash;
    private final List<AccountType> roles;
    private boolean verified;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!verified) {
            return List.of(UNVERIFIED_AUTHORITY);
        }

        return roles.stream()
                .map(AccountType::toAuthority)
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
