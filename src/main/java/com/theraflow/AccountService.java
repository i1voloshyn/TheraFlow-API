package com.theraflow;

import com.theraflow.mapper.DtoAccountMapper;
import com.theraflow.model.Account;
import com.theraflow.model.AccountRequest;
import com.theraflow.model.AccountResponse;
import com.theraflow.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final DtoAccountMapper mapper;

    public AccountResponse createAccount(AccountRequest request) {
        passwordValidator.validate(request.rawPassword());

        Account accountToSave = mapper.toAccount(
                request.email(),
                passwordEncoder.encode(request.rawPassword()),
                request.type()
        );

        Account createdAccount = accountRepository.create(accountToSave);

        return mapper.toResponse(createdAccount);
    }
}
