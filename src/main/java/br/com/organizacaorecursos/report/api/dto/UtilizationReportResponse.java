package br.com.organizacaorecursos.report.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UtilizationReportResponse(
        Instant startAt,
        Instant endAt,
        List<ResourceUtilization> resources
) {
    public record ResourceUtilization(
            UUID resourceId,
            String resourceName,
            BigDecimal allocatedHours,
            BigDecimal utilizationPercent,
            long avoidedConflicts
    ) {
    }
}