package br.com.organizacaorecursos.maintenance.application;

public class InvalidMaintenancePeriodException extends RuntimeException {

    public InvalidMaintenancePeriodException() {
        super("A data de término deve ser posterior à data de início.");
    }
}