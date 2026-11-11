package br.com.organizacaorecursos.report.api;

import br.com.organizacaorecursos.report.api.dto.UtilizationReportResponse;
import br.com.organizacaorecursos.report.application.UtilizationReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class UtilizationReportController {

    private final UtilizationReportService reportService;

    public UtilizationReportController(UtilizationReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/utilization")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSAVEL')")
    public UtilizationReportResponse utilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt,
            @RequestParam(required = false) UUID resourceId
    ) {
        return reportService.generate(startAt, endAt, resourceId);
    }
}