package com.theraflow.repository;

import com.theraflow.model.Account;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
@Repository
public class JdbcAccountRepository implements AccountRepository {
    private static final String INSERT_NEW_ACCOUNT_QUERY = """
            INSERT into accounts(email, password_hash, account_type)
            VALUES (:email, :password_hash, CAST(:account_type AS account_type))
            RETURNING id,created_at,updated_at
            """;

    private final NamedParameterJdbcTemplate namedTemplate;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Account create(Account account) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("email", account.getEmail())
                .addValue("password_hash", account.getPasswordHash())
                .addValue("account_type", account.getType().name().toLowerCase(Locale.ROOT));
        return transactionTemplate.execute((status) -> namedTemplate.query(
                INSERT_NEW_ACCOUNT_QUERY, parameterSource,
                (ResultSetExtractor<Account>) (resultSet) -> toCreatedAccount(resultSet, account)
        ));
    }

    private Instant toInstant(ResultSet rs, String columnLabel) throws SQLException {
        OffsetDateTime value = rs.getObject(columnLabel, OffsetDateTime.class);
        return value.toInstant();
    }

    private Account toCreatedAccount(ResultSet resultSet, Account account) throws SQLException {
        resultSet.next();
        UUID id = resultSet.getObject("id", UUID.class);
        Instant createdAt = toInstant(resultSet, "created_at");
        Instant updatedAt = toInstant(resultSet, "updated_at");
        return new Account(id, account.getEmail(), account.getPasswordHash(), account.getType(), createdAt, updatedAt);
    }
}

