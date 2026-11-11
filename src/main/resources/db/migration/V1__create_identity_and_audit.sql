CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

CREATE OR REPLACE FUNCTION audit_details_have_no_sensitive_keys(payload JSONB)
RETURNS BOOLEAN
LANGUAGE SQL
IMMUTABLE
AS $$
    WITH RECURSIVE nodes(value) AS (
        SELECT payload
        UNION ALL
        SELECT child.value
        FROM nodes
        CROSS JOIN LATERAL (
            SELECT value
            FROM jsonb_each(
                CASE
                    WHEN jsonb_typeof(nodes.value) = 'object' THEN nodes.value
                    ELSE '{}'::jsonb
                END
            )
            UNION ALL
            SELECT value
            FROM jsonb_array_elements(
                CASE
                    WHEN jsonb_typeof(nodes.value) = 'array' THEN nodes.value
                    ELSE '[]'::jsonb
                END
            )
        ) AS child
    )
    SELECT NOT EXISTS (
        SELECT 1
        FROM nodes
        CROSS JOIN LATERAL jsonb_object_keys(
            CASE
                WHEN jsonb_typeof(nodes.value) = 'object' THEN nodes.value
                ELSE '{}'::jsonb
            END
        ) AS key
        WHERE lower(key) LIKE '%password%'
           OR lower(key) LIKE '%token%'
           OR lower(key) LIKE '%secret%'
           OR lower(key) LIKE '%authorization%'
    );
$$;

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT fk_audit_events_actor FOREIGN KEY (actor_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_audit_events_details_object
        CHECK (jsonb_typeof(details) = 'object'),
    CONSTRAINT ck_audit_events_details_no_sensitive_keys
        CHECK (audit_details_have_no_sensitive_keys(details))
);

CREATE INDEX idx_audit_events_actor_id ON audit_events (actor_id);
CREATE INDEX idx_audit_events_occurred_at ON audit_events (occurred_at);
CREATE INDEX idx_audit_events_entity ON audit_events (entity_type, entity_id);

INSERT INTO roles (name)
VALUES ('SOLICITANTE'), ('RESPONSAVEL'), ('ADMINISTRADOR')
ON CONFLICT (name) DO NOTHING;
