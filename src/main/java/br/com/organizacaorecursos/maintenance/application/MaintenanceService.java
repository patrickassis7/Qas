package br.com.organizacaorecursos.maintenance.application;

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
import java.util.UUID;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final ResourceRepository resourceRepository;

    public MaintenanceService(
            MaintenanceBlockRepository maintenanceBlockRepository,
            ResourceRepository resourceRepository
    ) {
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.resourceRepository = resourceRepository;
    }

    public MaintenanceBlockEntity create(
            CreateMaintenanceBlockRequest request
    ) {

        if (!request.endAt().isAfter(request.startAt())) {
            throw new InvalidMaintenancePeriodException();
        }

        ResourceEntity resource = resourceRepository
                .findById(request.resourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(request.resourceId()));

        List<MaintenanceBlockEntity> existingBlocks =
                maintenanceBlockRepository.findByResourceId(
                        request.resourceId()
                );

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

        MaintenanceBlockEntity block =
                new MaintenanceBlockEntity(
                        resource,
                        request.startAt(),
                        request.endAt(),
                        request.reason(),
                        null
                );

        return maintenanceBlockRepository.save(block);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceBlockEntity> listByResource(
            UUID resourceId
    ) {
        return maintenanceBlockRepository.findByResourceId(
                resourceId
        );
    }

    public void cancel(UUID id) {

        MaintenanceBlockEntity block =
                maintenanceBlockRepository.findById(id)
                        .orElseThrow(() ->
                                new MaintenanceBlockNotFoundException(id));

        block.cancel();

        maintenanceBlockRepository.save(block);
    }
}