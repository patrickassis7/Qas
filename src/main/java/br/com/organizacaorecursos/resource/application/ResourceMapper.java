package br.com.organizacaorecursos.resource.application;

import br.com.organizacaorecursos.resource.api.dto.ResourceAvailabilityResponse;
import br.com.organizacaorecursos.resource.api.dto.ResourceResponse;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceResponse toResponse(ResourceEntity resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getType(),
                resource.getName(),
                resource.getLocation(),
                resource.getCapacity(),
                resource.getSkills(),
                resource.isRestricted(),
                resource.getResponsible() != null
                        ? resource.getResponsible().getId()
                        : null,
                resource.getStatus(),
                resource.getVersion(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }

    public ResourceAvailabilityResponse toAvailabilityResponse(
            ResourceEntity resource
    ) {
        return new ResourceAvailabilityResponse(
                resource.getId(),
                resource.getType(),
                resource.getName(),
                resource.getStatus(),
                resource.getStatus() == ResourceStatus.ATIVO
        );
    }
}