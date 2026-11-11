package br.com.organizacaorecursos.notification.application;

import br.com.organizacaorecursos.reservation.domain.ReservationEntity;

import java.time.Instant;
import java.util.UUID;

public record ReservationNotification(
        UUID reservationId,
        String recipientEmail,
        String action,
        Instant startAt,
        Instant endAt
) {
    public static ReservationNotification from(ReservationEntity reservation, String action) {
        return new ReservationNotification(
                reservation.getId(),
                reservation.getRequester().getEmail(),
                action,
                reservation.getStartAt(),
                reservation.getEndAt()
        );
    }
}