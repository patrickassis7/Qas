package br.com.organizacaorecursos.resource.infrastructure;

import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository
        extends JpaRepository<ResourceEntity, UUID>,
        JpaSpecificationExecutor<ResourceEntity> {

    Optional<ResourceEntity> findByName(String name);

    boolean existsByName(String name);

    // Ordem por id evita deadlock entre reservas concorrentes sobre os mesmos recursos.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResourceEntity r where r.id in :ids order by r.id")
    List<ResourceEntity> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}