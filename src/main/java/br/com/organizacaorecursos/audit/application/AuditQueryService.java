package br.com.organizacaorecursos.audit.application;

import br.com.organizacaorecursos.audit.api.dto.AuditEventResponse;
import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import br.com.organizacaorecursos.audit.infrastructure.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuditQueryService {

    private final AuditEventRepository auditEventRepository;

    public AuditQueryService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> consultar(
            UUID actorId, String action, String entityType, UUID entityId,
            Instant from, Instant to, int page, int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidAuditQueryException("Paginação inválida: use page >= 0 e size entre 1 e 100.");
        }
        if (from != null && to != null && !to.isAfter(from)) {
            throw new InvalidAuditQueryException("O fim do período deve ser posterior ao início.");
        }

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.asc("id")));
        Specification<AuditEventEntity> filters = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (actorId != null) {
                predicates.add(builder.equal(root.get("actor").get("id"), actorId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(builder.equal(root.get("action"), action));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(builder.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates.add(builder.equal(root.get("entityId"), entityId));
            }
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThan(root.get("occurredAt"), to));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return auditEventRepository.findAll(filters, pageable)
                .map(event -> new AuditEventResponse(
                        event.getId(),
                        event.getActor() == null ? null : event.getActor().getId(),
                        event.getAction(),
                        event.getEntityType(),
                        event.getEntityId(),
                        event.getOccurredAt(),
                        event.getDetails()
                ));
    }
}