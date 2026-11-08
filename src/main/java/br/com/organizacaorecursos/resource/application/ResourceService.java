package br.com.organizacaorecursos.resource.application;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.reservation.application.AgendaConflictChecker;
import br.com.organizacaorecursos.reservation.application.InvalidReservationPeriodException;
import br.com.organizacaorecursos.resource.api.dto.CreateResourceRequest;
import br.com.organizacaorecursos.resource.api.dto.ResourceSearchRequest;
import br.com.organizacaorecursos.resource.api.dto.UpdateResourceRequest;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import br.com.organizacaorecursos.resource.infrastructure.ResourceSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceValidator resourceValidator;
    private final AgendaConflictChecker agendaConflictChecker;

    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            ResourceValidator resourceValidator,
            AgendaConflictChecker agendaConflictChecker
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.resourceValidator = resourceValidator;
        this.agendaConflictChecker = agendaConflictChecker;
    }

    @Transactional(readOnly = true)
    public ResourceEntity findById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ResourceEntity> search(
            ResourceSearchRequest request,
            Pageable pageable
    ) {
        return resourceRepository.findAll(
                ResourceSpecifications.withFilters(request),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<ResourceEntity> availability() {
        return resourceRepository.findAll()
                .stream()
                .filter(resource ->
                        resource.getStatus() == ResourceStatus.ATIVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceEntity> availability(Instant startAt, Instant endAt) {
        List<ResourceEntity> active = availability();

        if (startAt == null && endAt == null) {
            return active;
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new InvalidReservationPeriodException(
                    "Informe startAt e endAt, com fim posterior ao início.");
        }

        return active.stream()
                .filter(resource -> agendaConflictChecker.isFree(resource.getId(), startAt, endAt))
                .toList();
    }

    public ResourceEntity create(CreateResourceRequest request) {

        resourceValidator.validate(request);

        if (resourceRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException(
                    request.name()
            );
        }

        UserEntity responsible = null;

        if (request.responsibleId() != null) {
            responsible = userRepository.findById(request.responsibleId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Responsável não encontrado."
                            ));
        }

        ResourceEntity resource = new ResourceEntity(
                request.type(),
                request.name(),
                request.location(),
                request.capacity(),
                request.skills(),
                request.restricted(),
                responsible
        );

        return resourceRepository.save(resource);
    }

    public ResourceEntity update(UUID id, UpdateResourceRequest request) {

        resourceValidator.validate(
                new CreateResourceRequest(
                        request.type(),
                        request.name(),
                        request.location(),
                        request.capacity(),
                        request.skills(),
                        request.restricted(),
                        request.responsibleId()
                )
        );

        ResourceEntity resource = findById(id);

        resourceRepository.findByName(request.name())
                .filter(found -> !found.getId().equals(resource.getId()))
                .ifPresent(found -> {
                    throw new ResourceAlreadyExistsException(
                            request.name()
                    );
                });

        UserEntity responsible = null;

        if (request.responsibleId() != null) {
            responsible = userRepository.findById(request.responsibleId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Responsável não encontrado."
                            ));
        }

        resource.update(
                request.type(),
                request.name(),
                request.location(),
                request.capacity(),
                request.skills(),
                request.restricted(),
                responsible
        );

        return resourceRepository.save(resource);
    }

    public void deactivate(UUID id) {
        ResourceEntity resource = findById(id);

        resource.deactivate();

        resourceRepository.save(resource);
    }
}