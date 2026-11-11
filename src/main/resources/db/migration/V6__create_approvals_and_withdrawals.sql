CREATE TABLE approvals (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reservation_id UUID NOT NULL,

    decided_by UUID NOT NULL,

    decision VARCHAR(20) NOT NULL,

    reason VARCHAR(500),

    decided_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_approvals_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_approvals_decided_by
        FOREIGN KEY (decided_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_approvals_decision
        CHECK (decision IN ('APROVADA', 'REJEITADA')),

    -- Uma única decisão por reserva: a segunda tentativa vira conflito.
    CONSTRAINT uk_approvals_reservation
        UNIQUE (reservation_id)
);

CREATE INDEX idx_approvals_decided_by
    ON approvals(decided_by);

CREATE TABLE withdrawal_records (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    reservation_id UUID NOT NULL,

    withdrawn_by UUID NOT NULL,

    withdrawn_at TIMESTAMPTZ NOT NULL,

    withdrawal_notes VARCHAR(500),

    returned_by UUID,

    returned_at TIMESTAMPTZ,

    return_notes VARCHAR(500),

    CONSTRAINT fk_withdrawal_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_withdrawal_withdrawn_by
        FOREIGN KEY (withdrawn_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_withdrawal_returned_by
        FOREIGN KEY (returned_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_withdrawal_reservation
        UNIQUE (reservation_id),

    CONSTRAINT ck_withdrawal_return_after_withdrawal
        CHECK (returned_at IS NULL OR returned_at >= withdrawn_at),

    CONSTRAINT ck_withdrawal_return_pair
        CHECK ((returned_at IS NULL) = (returned_by IS NULL))
);
