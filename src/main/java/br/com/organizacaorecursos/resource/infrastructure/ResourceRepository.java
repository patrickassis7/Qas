package br.com.organizacaorecursos.resource.infrastructure;

import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository
        extends JpaRepository<ResourceEntity, UUID>,
        JpaSpecificationExecutor<ResourceEntity> {

    Optional<ResourceEntity> findByName(String name);

    boolean existsByName(String name);
}