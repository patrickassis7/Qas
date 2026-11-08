package br.com.organizacaorecursos.maintenance.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateMaintenanceBlockRequest(

        @NotNull(message = "Informe o recurso.")
        UUID resourceId,

        @NotNull(message = "Informe a data de início.")
        @Future(message = "A data de início deve estar no futuro.")
        Instant startAt,

        @NotNull(message = "Informe a data de término.")
        @Future(message = "A data de término deve estar no futuro.")
        Instant endAt,

        @NotBlank(message = "Informe o motivo da manutenção.")
        String reason

) {
}