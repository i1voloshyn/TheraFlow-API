package com.theraflow.security;

import com.theraflow.repository.AccountRepository;
import com.theraflow.security.model.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountPrincipalService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    @NullMarked
    public AccountPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        var account =
                accountRepository.findAccountByEmail(username)
                        .orElseThrow
                                (() -> new UsernameNotFoundException(
                                        "Could not find an account with provided email: " + username));

        return new AccountPrincipal(
                account.getId(),
                account.getEmail(),
                List.of(account.getType()),
                account.getPasswordHash(),
                Boolean.TRUE.equals(account.getVerified()));
    }
}
