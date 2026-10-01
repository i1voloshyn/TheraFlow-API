package com.theraflow.security;

import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.model.TheraflowUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring where to find user data and load user records from DB
 */

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, username));
        return new TheraflowUser(
                account.getId(),
                account.getEmail(),
                account.getPasswordHash(),
                Boolean.TRUE.equals(account.getEmailVerified()),
                account.getType()
        );
    }
}
