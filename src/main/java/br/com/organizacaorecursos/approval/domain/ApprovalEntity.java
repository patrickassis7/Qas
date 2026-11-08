package br.com.organizacaorecursos.approval.domain;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approvals")
public class ApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by", nullable = false)
    private UserEntity decidedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecision decision;

    @Column(length = 500)
    private String reason;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private Instant decidedAt;

    protected ApprovalEntity() {
    }

    public ApprovalEntity(ReservationEntity reservation, UserEntity decidedBy,
                          ApprovalDecision decision, String reason) {
        this.reservation = reservation;
        this.decidedBy = decidedBy;
        this.decision = decision;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        decidedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ReservationEntity getReservation() {
        return reservation;
    }

    public UserEntity getDecidedBy() {
        return decidedBy;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
