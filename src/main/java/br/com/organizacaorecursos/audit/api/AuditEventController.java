package br.com.organizacaorecursos.audit.api;

import br.com.organizacaorecursos.audit.api.dto.AuditEventResponse;
import br.com.organizacaorecursos.audit.application.AuditQueryService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {

    private final AuditQueryService auditQueryService;

    public AuditEventController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<AuditEventResponse> list(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return auditQueryService.consultar(actorId, action, entityType, entityId, from, to, page, size);
    }
}