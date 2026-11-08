package br.com.organizacaorecursos.audit.infrastructure;

import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID>,
    JpaSpecificationExecutor<AuditEventEntity> {

    List<AuditEventEntity> findByActionAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
        String action, Instant from, Instant to
    );
}
