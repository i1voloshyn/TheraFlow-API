package com.theraflow.security.model;

import com.theraflow.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class AccountPrincipal implements UserDetails {
    @Getter
    private UUID accountId;
    private String email;
    private final List<AccountType> roles;
    private String passwordHash;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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

}
