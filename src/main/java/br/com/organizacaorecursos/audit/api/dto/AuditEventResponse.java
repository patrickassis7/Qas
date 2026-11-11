package br.com.organizacaorecursos.audit.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID actorId,
        String action,
        String entityType,
        UUID entityId,
        Instant occurredAt,
        JsonNode details
) {
}