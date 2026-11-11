package br.com.organizacaorecursos.resource.application;

import br.com.organizacaorecursos.resource.api.dto.CreateResourceRequest;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class ResourceValidator {

    public void validate(CreateResourceRequest request) {
        if (request.type() == null) {
            throw new IllegalArgumentException(
                    "Informe o tipo do recurso."
            );
        }

        switch (request.type()) {
            case SALA -> validateRoom(request);
            case PROFESSOR -> validateTeacher(request);
            case MATERIAL -> validateMaterial(request);
        }
    }

    private void validateRoom(CreateResourceRequest request) {
        if (request.location() == null || request.location().isBlank()) {
            throw new IllegalArgumentException(
                    "Sala deve possuir localização."
            );
        }

        if (request.capacity() == null || request.capacity() <= 0) {
            throw new IllegalArgumentException(
                    "Sala deve possuir capacidade maior que zero."
            );
        }
    }

    private void validateTeacher(CreateResourceRequest request) {
        if (request.skills() == null || request.skills().isBlank()) {
            throw new IllegalArgumentException(
                    "Professor deve possuir competências informadas."
            );
        }
    }

    private void validateMaterial(CreateResourceRequest request) {
        // D3: material é unidade exclusiva.
        // A validação funcional virá nos commits de reserva.
    }
}