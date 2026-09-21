package com.theraflow.account;

import com.theraflow.account.model.AccountType;
import com.theraflow.account.model.Account;
import com.theraflow.account.dto.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class DtoAccountMapper {

    public Account toAccount(String email, String passwordHash, AccountType type, String token) {
        return Account.builder()
                .id(null)
                .email(email)
                .passwordHash(passwordHash)
                .type(type)
                .verificationToken(token)
                .verified(false)
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
