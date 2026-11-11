package br.com.organizacaorecursos.reservation.application;

import br.com.organizacaorecursos.maintenance.infrastructure.MaintenanceBlockRepository;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import br.com.organizacaorecursos.reservation.infrastructure.ReservationRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class AgendaConflictChecker {

    private static final UUID NO_RESERVATION = new UUID(0L, 0L);
    private static final List<ReservationStatus> OCCUPYING = Arrays.stream(ReservationStatus.values())
            .filter(ReservationStatus::occupiesAgenda)
            .toList();

    private final ReservationRepository reservations;
    private final MaintenanceBlockRepository maintenanceBlocks;

    public AgendaConflictChecker(
            ReservationRepository reservations,
            MaintenanceBlockRepository maintenanceBlocks
    ) {
        this.reservations = reservations;
        this.maintenanceBlocks = maintenanceBlocks;
    }

    /** {@code ignoredReservationId} permite que uma alteração não conflite com a própria reserva. */
    public boolean isReserved(UUID resourceId, Instant startAt, Instant endAt, UUID ignoredReservationId) {
        return reservations.existsOverlapping(
                resourceId,
                OCCUPYING,
                startAt,
                endAt,
                ignoredReservationId == null ? NO_RESERVATION : ignoredReservationId
        );
    }

    public boolean isUnderMaintenance(UUID resourceId, Instant startAt, Instant endAt) {
        return maintenanceBlocks.existsActiveOverlap(resourceId, startAt, endAt);
    }

    public boolean isFree(UUID resourceId, Instant startAt, Instant endAt) {
        return !isReserved(resourceId, startAt, endAt, null)
                && !isUnderMaintenance(resourceId, startAt, endAt);
    }
}
