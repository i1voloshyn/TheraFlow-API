package com.theraflow.authentication;

import com.theraflow.authentication.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findAccountByEmail(String email);

    boolean existsByEmail(String email);
    @Query("SELECT a.emailVerified FROM Account a WHERE a.email = :email")
    boolean isEmailVerified(String email);

}

