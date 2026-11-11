package br.com.organizacaorecursos.maintenance.application;

import java.util.UUID;

public class MaintenanceBlockNotFoundException extends RuntimeException {

    public MaintenanceBlockNotFoundException(UUID id) {
        super("Bloqueio de manutenção não encontrado: " + id);
    }
}