package br.com.organizacaorecursos.audit.infrastructure;

import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends Repository<AuditEventEntity, UUID> {

    AuditEventEntity save(AuditEventEntity event);

    Optional<AuditEventEntity> findById(UUID id);

    long count();
}
