package br.com.organizacaorecursos.reservation.api.dto;

import br.com.organizacaorecursos.reservation.domain.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID requesterId,
        Instant startAt,
        Instant endAt,
        ReservationStatus status,
        String justification,
        List<UUID> resourceIds,
        Instant createdAt,
        Instant updatedAt
) {
}
