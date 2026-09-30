package com.theraflow.account;

import com.theraflow.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findAccountByEmail(String email);

    boolean existsByEmail(String email);

}

