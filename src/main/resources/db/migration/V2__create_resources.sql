CREATE TABLE resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    type VARCHAR(20) NOT NULL,

    name VARCHAR(150) NOT NULL,

    location VARCHAR(150),

    capacity INTEGER,

    skills TEXT,

    restricted BOOLEAN NOT NULL DEFAULT FALSE,

    responsible_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resources_responsible
        FOREIGN KEY (responsible_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_resources_type
        CHECK (type IN ('SALA', 'PROFESSOR', 'MATERIAL')),

    CONSTRAINT ck_resources_status
        CHECK (status IN ('ATIVO', 'MANUTENCAO', 'INATIVO')),

    CONSTRAINT ck_resources_capacity
        CHECK (capacity IS NULL OR capacity > 0),

    CONSTRAINT uk_resources_name
        UNIQUE (name)
);

CREATE INDEX idx_resources_type
    ON resources(type);

CREATE INDEX idx_resources_status
    ON resources(status);

CREATE INDEX idx_resources_location
    ON resources(location);

CREATE INDEX idx_resources_responsible
    ON resources(responsible_id);

CREATE INDEX idx_resources_name
    ON resources(name);