package br.com.organizacaorecursos.maintenance.domain;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maintenance_blocks")
public class MaintenanceBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceEntity resource;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceBlockStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MaintenanceBlockEntity() {
    }

    public MaintenanceBlockEntity(
            ResourceEntity resource,
            Instant startAt,
            Instant endAt,
            String reason,
            UserEntity createdBy
    ) {
        this.resource = resource;
        this.startAt = startAt;
        this.endAt = endAt;
        this.reason = reason;
        this.createdBy = createdBy;
        this.status = MaintenanceBlockStatus.ATIVO;
    }

    public void cancel() {
        this.status = MaintenanceBlockStatus.CANCELADO;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ResourceEntity getResource() {
        return resource;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public String getReason() {
        return reason;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public MaintenanceBlockStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}