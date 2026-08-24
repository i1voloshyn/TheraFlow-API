package com.theraflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NullMarked
@Entity
@Table(name = "therapists")
public final class Therapist {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID accountId;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    private String licenseNumber;
    @Nullable
    @Setter
    private String professionalTitle;
    @Nullable
    @Setter
    private String bio;
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false)
    @Nullable
    private Instant createdAt;
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false)
    @Nullable
    private Instant updatedAt;
}
