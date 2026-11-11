package br.com.organizacaorecursos.resource.api.dto;

import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.domain.ResourceType;

import java.util.UUID;

public record ResourceAvailabilityResponse(
        UUID id,
        ResourceType type,
        String name,
        ResourceStatus status,
        boolean available
) {
}