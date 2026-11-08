package br.com.organizacaorecursos.reservation.domain;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(length = 500)
    private String justification;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reservation_resources",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private Set<ResourceEntity> resources = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReservationEntity() {
    }

    public ReservationEntity(
            UserEntity requester,
            Instant startAt,
            Instant endAt,
            String justification,
            Collection<ResourceEntity> resources
    ) {
        this.requester = requester;
        this.startAt = startAt;
        this.endAt = endAt;
        this.justification = justification;
        this.resources.addAll(resources);
        this.status = ReservationStatus.SOLICITADA;
    }

    public void reschedule(
            Instant startAt,
            Instant endAt,
            String justification,
            Collection<ResourceEntity> resources
    ) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.justification = justification;
        this.resources.clear();
        this.resources.addAll(resources);
    }

    public void transitionTo(ReservationStatus target) {
        ReservationStateMachine.requireTransition(status, target);
        this.status = target;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getRequester() {
        return requester;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getJustification() {
        return justification;
    }

    public Set<ResourceEntity> getResources() {
        return resources;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
