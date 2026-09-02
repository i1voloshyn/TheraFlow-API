CREATE TYPE account_type AS ENUM ('THERAPIST', 'GUARDIAN');

CREATE TABLE accounts
(
    id            UUID                              DEFAULT gen_random_uuid(),
    email         TEXT                     NOT NULL,
    password_hash TEXT                     NOT NULL,
    account_type  account_type             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_email
        UNIQUE (email),
    CONSTRAINT ck_accounts_email_normalized
        CHECK (email = lower(trim(email)) AND email <> ''),
    CONSTRAINT ck_accounts_password_hash_not_blank
        CHECK (trim(password_hash) <> '')
);

CREATE TABLE therapists
(
    id                 UUID                              DEFAULT gen_random_uuid(),
    account_id         UUID                     NOT NULL,
    first_name         TEXT                     NOT NULL,
    last_name          TEXT                     NOT NULL,
    license_number     TEXT                     NOT NULL,
    professional_title TEXT,
    about              JSONB                             DEFAULT '{}'::jsonb,
    address            JSONB                             DEFAULT '[]'::jsonb,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_therapists PRIMARY KEY (id),

    CONSTRAINT fk_therapists_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT uq_account_id UNIQUE (account_id),

    CONSTRAINT ck_therapists_first_name_not_blank CHECK (first_name = trim(first_name) AND first_name <> ''),
    CONSTRAINT ck_therapists_last_name_not_blank CHECK (last_name = trim(last_name) AND last_name <> ''),
    CONSTRAINT ck_therapists_license_number_normalized
        CHECK (
            license_number = trim(license_number)
                AND license_number <> ''
            ),
    CONSTRAINT ck_therapists_professional_title_not_blank
        CHECK (
            professional_title IS NULL
                OR trim(professional_title) <> ''
            ),

    CONSTRAINT uq_therapists_license_number UNIQUE (license_number)
);
