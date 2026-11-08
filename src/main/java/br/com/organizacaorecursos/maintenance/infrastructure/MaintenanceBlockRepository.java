package br.com.organizacaorecursos.maintenance.infrastructure;

import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceBlockRepository
        extends JpaRepository<MaintenanceBlockEntity, UUID> {

    List<MaintenanceBlockEntity> findByResourceId(UUID resourceId);

    Optional<MaintenanceBlockEntity> findById(UUID id);
}