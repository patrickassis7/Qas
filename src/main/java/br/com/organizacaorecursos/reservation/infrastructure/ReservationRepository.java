package br.com.organizacaorecursos.reservation.infrastructure;

import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID>,
        JpaSpecificationExecutor<ReservationEntity> {

    Page<ReservationEntity> findByRequesterId(UUID requesterId, Pageable pageable);

    // Serializa transições de estado concorrentes sobre a mesma reserva.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservationEntity r where r.id = :id")
    Optional<ReservationEntity> findByIdForUpdate(@Param("id") UUID id);

    /** Sobreposição semiaberta: existente.start < novo.end && novo.start < existente.end. */
    @Query("""
            select count(r) > 0
            from ReservationEntity r join r.resources res
            where res.id = :resourceId
              and r.status in :statuses
              and r.startAt < :endAt
              and r.endAt > :startAt
              and r.id <> :excludeId
            """)
    boolean existsOverlapping(
            @Param("resourceId") UUID resourceId,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt,
            @Param("excludeId") UUID excludeId
    );
}
