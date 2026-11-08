package br.com.organizacaorecursos.report.application;

import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import br.com.organizacaorecursos.audit.infrastructure.AuditEventRepository;
import br.com.organizacaorecursos.report.api.dto.UtilizationReportResponse;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import br.com.organizacaorecursos.reservation.infrastructure.ReservationRepository;
import br.com.organizacaorecursos.resource.application.ResourceNotFoundException;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UtilizationReportService {

    private static final List<ReservationStatus> ALLOCATED_STATUSES = List.of(
            ReservationStatus.APROVADA, ReservationStatus.EM_USO, ReservationStatus.CONCLUIDA
    );
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);

    private final ReservationRepository reservations;
    private final ResourceRepository resources;
    private final AuditEventRepository auditEvents;

    public UtilizationReportService(
            ReservationRepository reservations,
            ResourceRepository resources,
            AuditEventRepository auditEvents
    ) {
        this.reservations = reservations;
        this.resources = resources;
        this.auditEvents = auditEvents;
    }

    @Transactional(readOnly = true)
    public UtilizationReportResponse generate(Instant startAt, Instant endAt, UUID resourceId) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new InvalidReportPeriodException("O fim do período deve ser posterior ao início.");
        }

        List<ResourceEntity> reportResources = resourceId == null
                ? resources.findAll()
                : List.of(resources.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException(resourceId)));
        Set<UUID> reportResourceIds = new HashSet<>();
        reportResources.forEach(resource -> reportResourceIds.add(resource.getId()));

        List<ReservationEntity> allocated = reservations.findAll(allocationFilter(startAt, endAt, resourceId));
        Map<UUID, BigDecimal> allocatedSeconds = new HashMap<>();
        for (ReservationEntity reservation : allocated) {
            Instant overlapStart = reservation.getStartAt().isAfter(startAt) ? reservation.getStartAt() : startAt;
            Instant overlapEnd = reservation.getEndAt().isBefore(endAt) ? reservation.getEndAt() : endAt;
            BigDecimal seconds = durationInSeconds(overlapStart, overlapEnd);
            reservation.getResources().stream()
                    .map(ResourceEntity::getId)
                    .filter(reportResourceIds::contains)
                    .forEach(id -> allocatedSeconds.merge(id, seconds, BigDecimal::add));
        }

        Map<UUID, Long> avoidedConflicts = countAvoidedConflicts(startAt, endAt, reportResourceIds);
        BigDecimal periodSeconds = durationInSeconds(startAt, endAt);
        List<UtilizationReportResponse.ResourceUtilization> results = reportResources.stream()
                .map(resource -> {
                    BigDecimal seconds = allocatedSeconds.getOrDefault(resource.getId(), BigDecimal.ZERO);
                    BigDecimal hours = seconds.divide(SECONDS_PER_HOUR, 2, RoundingMode.HALF_UP);
                    BigDecimal utilization = seconds.multiply(BigDecimal.valueOf(100))
                            .divide(periodSeconds, 2, RoundingMode.HALF_UP);
                    return new UtilizationReportResponse.ResourceUtilization(
                            resource.getId(), resource.getName(), hours, utilization,
                            avoidedConflicts.getOrDefault(resource.getId(), 0L));
                })
                .toList();

        return new UtilizationReportResponse(startAt, endAt, results);
    }

    private static Specification<ReservationEntity> allocationFilter(
            Instant startAt, Instant endAt, UUID resourceId
    ) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(ALLOCATED_STATUSES));
            predicates.add(builder.lessThan(root.get("startAt"), endAt));
            predicates.add(builder.greaterThan(root.get("endAt"), startAt));
            if (resourceId != null) {
                predicates.add(builder.equal(root.join("resources").get("id"), resourceId));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Map<UUID, Long> countAvoidedConflicts(
            Instant startAt, Instant endAt, Set<UUID> reportResourceIds
    ) {
        Map<UUID, Long> counts = new HashMap<>();
        List<AuditEventEntity> events = auditEvents
                .findByActionAndOccurredAtGreaterThanEqualAndOccurredAtLessThan("CONFLITO_EVITADO", startAt, endAt);
        for (AuditEventEntity event : events) {
            JsonNode resourceIds = event.getDetails().get("resourceIds");
            if (resourceIds == null || !resourceIds.isArray()) {
                continue;
            }
            Set<UUID> resourcesInEvent = new HashSet<>();
            resourceIds.forEach(value -> {
                if (value.isTextual()) {
                    try {
                        resourcesInEvent.add(UUID.fromString(value.asText()));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore unrelated or malformed legacy detail values.
                    }
                }
            });
            resourcesInEvent.stream()
                    .filter(reportResourceIds::contains)
                    .forEach(id -> counts.merge(id, 1L, Long::sum));
        }
        return counts;
    }

    private static BigDecimal durationInSeconds(Instant startAt, Instant endAt) {
        Duration duration = Duration.between(startAt, endAt);
        return BigDecimal.valueOf(duration.getSeconds())
                .add(BigDecimal.valueOf(duration.getNano(), 9));
    }
}