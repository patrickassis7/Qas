package br.com.organizacaorecursos.maintenance.infrastructure;

import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockEntity;
import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceBlockRepository
        extends JpaRepository<MaintenanceBlockEntity, UUID> {

    List<MaintenanceBlockEntity> findByResourceId(UUID resourceId);

    Optional<MaintenanceBlockEntity> findById(UUID id);

    @Query("""
            select count(b) > 0
            from MaintenanceBlockEntity b
            where b.resource.id = :resourceId
              and b.status = :status
              and b.startAt < :endAt
              and b.endAt > :startAt
            """)
    boolean existsOverlapping(
            @Param("resourceId") UUID resourceId,
            @Param("status") MaintenanceBlockStatus status,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt
    );

    default boolean existsActiveOverlap(UUID resourceId, Instant startAt, Instant endAt) {
        return existsOverlapping(resourceId, MaintenanceBlockStatus.ATIVO, startAt, endAt);
    }
}