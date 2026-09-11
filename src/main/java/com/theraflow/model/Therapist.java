package com.theraflow.model;

import com.theraflow.model.about.About;
import com.theraflow.model.about.Address;
import com.theraflow.model.account.Account;
import com.theraflow.model.patient.Patient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "patients")
@Builder
@NullMarked
@Entity
@Table(name = "therapists")
public class Therapist {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne
    private Account account;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private String licenseNumber; //PL PWZ ?
    @Nullable
    @Setter
    private String professionalTitle;
    @Nullable
    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    private About about;
    @Nullable
    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Address> address;
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false)
    @Nullable
    private Instant createdAt;
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false)
    @Nullable
    private Instant updatedAt;

    @OneToMany(mappedBy = "therapist")
    @Builder.Default
    private Set<Patient> patients = new HashSet<>();

    public void addPatient(Patient patient) {
        if (patients.add(patient)) {
            patient.setTherapist(this);
        }

    }

}
