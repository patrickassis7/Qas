ALTER TABLE maintenance_blocks
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ATIVO';

ALTER TABLE maintenance_blocks
    ADD CONSTRAINT ck_maintenance_status
        CHECK (status IN ('ATIVO', 'CANCELADO'));

ALTER TABLE maintenance_blocks
    ALTER COLUMN created_by DROP NOT NULL;

CREATE INDEX idx_maintenance_status
    ON maintenance_blocks(status);
