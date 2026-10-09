package com.theraflow.authentication;

import com.theraflow.authentication.model.AccountType;
import com.theraflow.authentication.model.Account;
import com.theraflow.authentication.dto.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class DtoAccountMapper {

    public Account toAccount(String email, String passwordHash, AccountType type) {
        return Account.builder()
                .id(null)
                .email(email)
                .passwordHash(passwordHash)
                .type(type)
                .emailVerified(false)
                .createdAt(null)
                .updatedAt(null)
                .build();
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
