package br.com.organizacaorecursos.resource.api.dto;

import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.domain.ResourceType;

public record ResourceSearchRequest(

        ResourceType type,

        String location,

        Integer minimumCapacity,

        String skill,

        ResourceStatus status

) {
}