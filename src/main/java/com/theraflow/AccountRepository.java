package com.theraflow;

import com.theraflow.model.Account;

public interface AccountRepository {
    Account save(Account account);
}
