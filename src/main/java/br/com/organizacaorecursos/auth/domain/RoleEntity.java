package br.com.organizacaorecursos.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RoleEntity() {
    }

    public RoleEntity(String name) {
        this.name = name;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
