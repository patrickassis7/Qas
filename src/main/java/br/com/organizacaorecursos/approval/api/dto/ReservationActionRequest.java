package br.com.organizacaorecursos.approval.api.dto;

import jakarta.validation.constraints.Size;

public record ReservationActionRequest(@Size(max = 500) String note) {
}
