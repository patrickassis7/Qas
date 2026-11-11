package br.com.organizacaorecursos.maintenance.application;

import br.com.organizacaorecursos.audit.application.AuditService;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.maintenance.api.dto.CreateMaintenanceBlockRequest;
import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockEntity;
import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockStatus;
import br.com.organizacaorecursos.maintenance.infrastructure.MaintenanceBlockRepository;
import br.com.organizacaorecursos.resource.application.ResourceNotFoundException;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final ResourceRepository resourceRepository;
        private final UserRepository userRepository;
        private final AuditService auditService;

    public MaintenanceService(
            MaintenanceBlockRepository maintenanceBlockRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public MaintenanceBlockEntity create(
            UUID actorId,
            CreateMaintenanceBlockRequest request
    ) {

        if (!request.endAt().isAfter(request.startAt())) {
            throw new InvalidMaintenancePeriodException();
        }

        ResourceEntity resource = resourceRepository
                .findById(request.resourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(request.resourceId()));
        UserEntity actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));

        List<MaintenanceBlockEntity> existingBlocks =
                maintenanceBlockRepository.findByResourceId(request.resourceId());

        boolean overlap = existingBlocks.stream()
                .filter(block ->
                        block.getStatus() == MaintenanceBlockStatus.ATIVO)
                .anyMatch(block ->
                        request.startAt().isBefore(block.getEndAt())
                                && block.getStartAt().isBefore(request.endAt())
                );

        if (overlap) {
            throw new MaintenanceOverlapException();
        }

        MaintenanceBlockEntity block = new MaintenanceBlockEntity(
                resource,
                request.startAt(),
                request.endAt(),
                request.reason(),
                actor
        );

        MaintenanceBlockEntity saved = maintenanceBlockRepository.save(block);
        auditService.registrar(actorId, "MANUTENCAO_CRIADA", "MAINTENANCE_BLOCK", saved.getId(), details(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceBlockEntity> listByResource(
            UUID resourceId
    ) {
        return maintenanceBlockRepository.findByResourceId(
                resourceId
        );
    }

        public void cancel(UUID id, UUID actorId) {

        MaintenanceBlockEntity block =
                maintenanceBlockRepository.findById(id)
                        .orElseThrow(() ->
                                new MaintenanceBlockNotFoundException(id));

        block.cancel();

        maintenanceBlockRepository.save(block);
        auditService.registrar(actorId, "MANUTENCAO_CANCELADA", "MAINTENANCE_BLOCK",
                block.getId(), details(block));
        }

    private static Map<String, Object> details(MaintenanceBlockEntity block) {
        return Map.of(
                "resourceId", block.getResource().getId().toString(),
                "startAt", block.getStartAt().toString(),
                "endAt", block.getEndAt().toString(),
                "status", block.getStatus().name()
        );
    }
}