package br.com.organizacaorecursos.resource.api.dto;

import br.com.organizacaorecursos.resource.domain.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateResourceRequest(

        @NotNull(message = "Informe o tipo do recurso.")
        ResourceType type,

        @NotBlank(message = "Informe o nome do recurso.")
        String name,

        String location,

        Integer capacity,

        String skills,

        boolean restricted,

        UUID responsibleId

) {
}