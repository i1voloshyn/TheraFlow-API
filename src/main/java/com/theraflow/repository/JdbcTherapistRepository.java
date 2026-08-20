package com.theraflow.repository;

import com.theraflow.model.Therapist;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class JdbcTherapistRepository implements TherapistRepository {

    private static final String INSERT_NEW_THERAPIST_QUERY = """
            INSERT INTO therapists (account_id, first_name, last_name, license_number, professional_title, bio) 
            VALUES (:account_id, :first_name, :last_name, :license_number, :professional_title, :bio)
            RETURNING id, created_at,updated_at
            """;

    private static final String UPDATE_THERAPIST_QUERY = """
            UPDATE therapists
            SET first_name = :first_name,
                last_name = :last_name,
                professional_title = :professional_title,
                bio = :bio
            WHERE id = :id
            RETURNING id,first_name,last_name, license_number,professional_title,bio,created_at,updated_at;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Therapist createProfile(Therapist therapist) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("account_id", therapist.getAccountId())
                .addValue("first_name", therapist.getFirstName())
                .addValue("last_name", therapist.getLastName())
                .addValue("license_number", therapist.getLicenseNumber())
                .addValue("professional_title", therapist.getProfessionalTitle())
                .addValue("bio", therapist.getBio());
        return jdbcTemplate.query(INSERT_NEW_THERAPIST_QUERY,
                parameterSource,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return toCreatedTherapist(resultSet, therapist);
                    } else throw new DataIntegrityViolationException("Error while trying to create profile");
                }
        );
    }

    @Override
    public Therapist updateProfile(Therapist therapist) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", therapist.getId())
                .addValue("first_name", therapist.getFirstName())
                .addValue("last_name", therapist.getLastName())
                .addValue("professional_title", therapist.getProfessionalTitle())
                .addValue("bio", therapist.getBio());
        return jdbcTemplate.query(UPDATE_THERAPIST_QUERY,
                parameterSource,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return toCreatedTherapist(resultSet, therapist);
                    } else throw new DataIntegrityViolationException("Error while trying to update profile");
                });
    }

    private Therapist toCreatedTherapist(ResultSet rs, Therapist therapist) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        Instant createdAt = toInstant(rs, "created_at");
        Instant updatedAt = toInstant(rs, "updated_at");

        return new Therapist(id,
                therapist.getAccountId(),
                therapist.getFirstName(),
                therapist.getLastName(),
                therapist.getLicenseNumber(),
                therapist.getProfessionalTitle(),
                therapist.getBio(),
                createdAt,
                updatedAt);
    }

    private Instant toInstant(ResultSet rs, String columnLabel) throws SQLException {
        OffsetDateTime value = rs.getObject(columnLabel, OffsetDateTime.class);
        return value.toInstant();
    }
}
