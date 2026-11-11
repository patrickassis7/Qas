package br.com.organizacaorecursos.maintenance.api;

import br.com.organizacaorecursos.maintenance.api.dto.CreateMaintenanceBlockRequest;
import br.com.organizacaorecursos.maintenance.api.dto.MaintenanceBlockResponse;
import br.com.organizacaorecursos.maintenance.application.MaintenanceMapper;
import br.com.organizacaorecursos.maintenance.application.MaintenanceService;
import br.com.organizacaorecursos.shared.infrastructure.web.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-blocks")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final MaintenanceMapper maintenanceMapper;

    public MaintenanceController(
            MaintenanceService maintenanceService,
            MaintenanceMapper maintenanceMapper
    ) {
        this.maintenanceService = maintenanceService;
        this.maintenanceMapper = maintenanceMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MaintenanceBlockResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateMaintenanceBlockRequest request
    ) {
        return maintenanceMapper.toResponse(
                maintenanceService.create(CurrentUser.id(authentication), request)
        );
    }

    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<MaintenanceBlockResponse> listByResource(
            @PathVariable UUID resourceId
    ) {
        return maintenanceService.listByResource(resourceId)
                .stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void cancel(Authentication authentication, @PathVariable UUID id) {
        maintenanceService.cancel(id, CurrentUser.id(authentication));
    }
}