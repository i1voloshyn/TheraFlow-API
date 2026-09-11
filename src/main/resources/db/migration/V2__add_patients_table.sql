CREATE TYPE sex AS ENUM ('MALE', 'FEMALE', 'UNKNOWN');

CREATE TABLE patients
(
    id            UUID                              DEFAULT gen_random_uuid(),
    therapist_id  UUID                     NOT NULL,
    first_name    TEXT                     NOT NULL,
    last_name     TEXT                     NOT NULL,
    sex           sex                      NOT NULL,
    pesel         CHAR(11)                 NOT NULL,
    date_of_birth DATE                     NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_patients PRIMARY KEY (id),

    CONSTRAINT ck_patients_first_name_not_blank CHECK (first_name = trim(first_name) AND first_name <> ''),
    CONSTRAINT ck_patients_last_name_not_blank CHECK (last_name = trim(last_name) AND last_name <> ''),
    CONSTRAINT ck_patients_pesel_format CHECK (pesel ~ '^[0-9]{11}$'),
    CONSTRAINT ck_patients_date_of_birth_not_future CHECK (date_of_birth <= CURRENT_DATE),

    CONSTRAINT fk_patients_therapist FOREIGN KEY (therapist_id) REFERENCES therapists (id) ON DELETE RESTRICT,
    CONSTRAINT uk_patients_therapist_pesel UNIQUE (therapist_id, pesel)

);

