package com.theraflow.mapper;

import com.theraflow.model.AccountType;
import com.theraflow.model.Account;
import com.theraflow.dto.AccountResponse;
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
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    public AccountResponse toResponse(Account account, String token) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                token,
                account.getType(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

}
