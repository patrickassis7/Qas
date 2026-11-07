package br.com.organizacaorecursos.resource.infrastructure;

import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<ResourceEntity, UUID> {
}