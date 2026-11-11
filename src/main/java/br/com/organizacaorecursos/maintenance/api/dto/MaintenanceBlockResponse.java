package br.com.organizacaorecursos.maintenance.api.dto;

import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockStatus;

import java.time.Instant;
import java.util.UUID;

public record MaintenanceBlockResponse(

        UUID id,

        UUID resourceId,

        Instant startAt,

        Instant endAt,

        String reason,

        UUID createdBy,

        MaintenanceBlockStatus status,

        Instant createdAt

) {
}