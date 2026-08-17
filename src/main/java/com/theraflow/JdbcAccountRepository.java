package com.theraflow;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
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
            RETURNING id,email,password_hash, account_type,created_at,updated_at
            """;

    private final NamedParameterJdbcTemplate namedTemplate;
    private final TransactionTemplate transactionTemplate;

    private final RowMapper<Account> accMapper = ((rs, rowNum) -> {
        UUID id = rs.getObject("id", UUID.class);
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        AccountType type = toAccountType(rs);
        Instant createdAt = toInstant(rs, "created_at");
        Instant updatedAt = toInstant(rs, "updated_at");
        return new Account(id, email, passwordHash, type, createdAt, updatedAt);
    });

    @Override
    public Account save(Account account) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("email", account.getEmail())
                .addValue("password_hash", account.getPasswordHash())
                .addValue("account_type", account.getType().name().toLowerCase(Locale.ROOT));
        return transactionTemplate.execute((status) -> namedTemplate.queryForObject(
                INSERT_NEW_ACCOUNT_QUERY, parameterSource, accMapper
        ));
    }

    private Instant toInstant(ResultSet rs, String columnLabel) throws SQLException {
        OffsetDateTime value = rs.getObject(columnLabel, OffsetDateTime.class);
        return value.toInstant();
    }

    private AccountType toAccountType(ResultSet rs) throws SQLException {
        String value = rs.getString("account_type");
        return AccountType.valueOf(value.toUpperCase(Locale.ROOT));
    }

}

