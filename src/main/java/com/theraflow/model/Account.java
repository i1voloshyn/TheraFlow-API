package com.theraflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@NullMarked
@Builder
@Getter
@Entity
@Table(name = "accounts")
public class Account {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Email
    @Column(unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    @Setter
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AccountType type;
    @Column(name = "verification_token")
    @Setter
    @Nullable
    private String verificationToken;
    @Nullable
    @Setter
    private Boolean verified;
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false)
    @Nullable
    private Instant createdAt;
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false)
    @Nullable
    private Instant updatedAt;
}
