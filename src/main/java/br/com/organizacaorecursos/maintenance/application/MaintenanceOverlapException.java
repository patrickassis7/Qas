package br.com.organizacaorecursos.maintenance.application;

public class MaintenanceOverlapException extends RuntimeException {

    public MaintenanceOverlapException() {
        super("Já existe um bloqueio de manutenção para o período informado.");
    }
}