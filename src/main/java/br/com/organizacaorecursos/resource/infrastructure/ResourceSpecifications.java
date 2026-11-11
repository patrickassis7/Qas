package br.com.organizacaorecursos.resource.infrastructure;

import br.com.organizacaorecursos.resource.api.dto.ResourceSearchRequest;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import org.springframework.data.jpa.domain.Specification;

public final class ResourceSpecifications {

    private ResourceSpecifications() {
    }

    public static Specification<ResourceEntity> withFilters(
            ResourceSearchRequest request
    ) {

        Specification<ResourceEntity> specification =
                Specification.where(null);

        if (request.type() != null) {
            specification = specification.and(
                    (root, query, builder) ->
                            builder.equal(
                                    root.get("type"),
                                    request.type()
                            )
            );
        }

        if (request.location() != null
                && !request.location().isBlank()) {

            specification = specification.and(
                    (root, query, builder) ->
                            builder.like(
                                    builder.lower(root.get("location")),
                                    "%" + request.location().toLowerCase() + "%"
                            )
            );
        }

        if (request.minimumCapacity() != null) {

            specification = specification.and(
                    (root, query, builder) ->
                            builder.greaterThanOrEqualTo(
                                    root.get("capacity"),
                                    request.minimumCapacity()
                            )
            );
        }

        if (request.skill() != null
                && !request.skill().isBlank()) {

            specification = specification.and(
                    (root, query, builder) ->
                            builder.like(
                                    builder.lower(root.get("skills")),
                                    "%" + request.skill().toLowerCase() + "%"
                            )
            );
        }

        if (request.status() != null) {

            specification = specification.and(
                    (root, query, builder) ->
                            builder.equal(
                                    root.get("status"),
                                    request.status()
                            )
            );
        }

        return specification;
    }
}