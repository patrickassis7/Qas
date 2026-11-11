package br.com.organizacaorecursos.reservation.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateReservationRequest(

        @NotNull Instant startAt,

        @NotNull Instant endAt,

        @NotEmpty Set<@NotNull UUID> resourceIds,

        @Size(max = 500) String justification

) {
}
