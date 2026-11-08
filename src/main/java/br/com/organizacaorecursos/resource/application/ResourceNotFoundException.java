package br.com.organizacaorecursos.resource.application;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(UUID resourceId) {
        super("Recurso não encontrado: " + resourceId);
    }
}