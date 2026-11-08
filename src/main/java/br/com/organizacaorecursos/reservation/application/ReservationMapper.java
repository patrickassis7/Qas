package br.com.organizacaorecursos.reservation.application;

import br.com.organizacaorecursos.reservation.api.dto.ReservationResponse;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(ReservationEntity reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRequester().getId(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getStatus(),
                reservation.getJustification(),
                reservation.getResources().stream()
                        .map(ResourceEntity::getId)
                        .sorted(Comparator.naturalOrder())
                        .toList(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
