package br.com.organizacaorecursos.approval.domain;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_records")
public class WithdrawalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "withdrawn_by", nullable = false)
    private UserEntity withdrawnBy;

    @Column(name = "withdrawn_at", nullable = false)
    private Instant withdrawnAt;

    @Column(name = "withdrawal_notes", length = 500)
    private String withdrawalNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returned_by")
    private UserEntity returnedBy;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Column(name = "return_notes", length = 500)
    private String returnNotes;

    protected WithdrawalRecordEntity() {
    }

    public WithdrawalRecordEntity(ReservationEntity reservation, UserEntity withdrawnBy,
                                  Instant withdrawnAt, String notes) {
        this.reservation = reservation;
        this.withdrawnBy = withdrawnBy;
        this.withdrawnAt = withdrawnAt;
        this.withdrawalNotes = notes;
    }

    public void registerReturn(UserEntity returnedBy, Instant returnedAt, String notes) {
        this.returnedBy = returnedBy;
        this.returnedAt = returnedAt;
        this.returnNotes = notes;
    }

    public boolean isReturned() {
        return returnedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public ReservationEntity getReservation() {
        return reservation;
    }

    public UserEntity getWithdrawnBy() {
        return withdrawnBy;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    public String getWithdrawalNotes() {
        return withdrawalNotes;
    }

    public UserEntity getReturnedBy() {
        return returnedBy;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public String getReturnNotes() {
        return returnNotes;
    }
}
