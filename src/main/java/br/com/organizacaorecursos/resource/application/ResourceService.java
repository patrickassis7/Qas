package br.com.organizacaorecursos.resource.application;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.resource.api.dto.CreateResourceRequest;
import br.com.organizacaorecursos.resource.api.dto.UpdateResourceRequest;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceValidator resourceValidator;

    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            ResourceValidator resourceValidator
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.resourceValidator = resourceValidator;
    }

    @Transactional(readOnly = true)
    public ResourceEntity findById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public ResourceEntity create(CreateResourceRequest request) {

        resourceValidator.validate(request);

        if (resourceRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException(request.name());
        }

        UserEntity responsible = null;

        if (request.responsibleId() != null) {
            responsible = userRepository.findById(request.responsibleId())
                    .orElseThrow(() -> new IllegalArgumentException(
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
                    throw new ResourceAlreadyExistsException(request.name());
                });

        UserEntity responsible = null;

        if (request.responsibleId() != null) {
            responsible = userRepository.findById(request.responsibleId())
                    .orElseThrow(() -> new IllegalArgumentException(
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