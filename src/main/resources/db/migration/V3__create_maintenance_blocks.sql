CREATE TABLE maintenance_blocks (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    resource_id UUID NOT NULL,

    start_at TIMESTAMPTZ NOT NULL,

    end_at TIMESTAMPTZ NOT NULL,

    reason VARCHAR(500) NOT NULL,

    created_by UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_maintenance_resource
        FOREIGN KEY (resource_id)
        REFERENCES resources(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_maintenance_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_maintenance_period
        CHECK (end_at > start_at)
);

CREATE INDEX idx_maintenance_resource
    ON maintenance_blocks(resource_id);

CREATE INDEX idx_maintenance_period
    ON maintenance_blocks(start_at, end_at);

CREATE INDEX idx_maintenance_created_by
    ON maintenance_blocks(created_by);