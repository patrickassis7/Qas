package br.com.organizacaorecursos.resource.api.dto;

import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        ResourceType type,
        String name,
        String location,
        Integer capacity,
        String skills,
        boolean restricted,
        UUID responsibleId,
        ResourceStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}