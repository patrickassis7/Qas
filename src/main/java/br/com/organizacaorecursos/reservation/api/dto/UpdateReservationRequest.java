package br.com.organizacaorecursos.reservation.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Campos ausentes mantêm o valor atual da reserva. */
public record UpdateReservationRequest(

        Instant startAt,

        Instant endAt,

        @Size(min = 1) Set<@NotNull UUID> resourceIds,

        @Size(max = 500) String justification

) {
}
