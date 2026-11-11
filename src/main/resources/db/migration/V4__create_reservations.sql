CREATE TABLE reservations (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    requester_id UUID NOT NULL,

    start_at TIMESTAMPTZ NOT NULL,

    end_at TIMESTAMPTZ NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA',

    justification VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservations_requester
        FOREIGN KEY (requester_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_reservations_period
        CHECK (end_at > start_at),

    CONSTRAINT ck_reservations_status
        CHECK (status IN (
            'SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA',
            'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU'
        ))
);

CREATE INDEX idx_reservations_requester
    ON reservations(requester_id);

CREATE INDEX idx_reservations_period
    ON reservations(start_at, end_at);

CREATE INDEX idx_reservations_status
    ON reservations(status);

CREATE TABLE reservation_resources (

    reservation_id UUID NOT NULL,

    resource_id UUID NOT NULL,

    PRIMARY KEY (reservation_id, resource_id),

    CONSTRAINT fk_reservation_resources_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reservation_resources_resource
        FOREIGN KEY (resource_id)
        REFERENCES resources(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_reservation_resources_resource
    ON reservation_resources(resource_id);
