package br.com.organizacaorecursos.resource.application;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String name) {
        super("Já existe um recurso com o nome: " + name);
    }
}