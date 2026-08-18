package com.theraflow.mapper;

import com.theraflow.AccountType;
import com.theraflow.model.Account;
import com.theraflow.model.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class DtoAccountMapper {

    public Account toAccount(String email, String passwordHash, AccountType type) {
        return new Account(
                null,
                email,
                passwordHash,
                type,
                null, null
        );
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getType(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
