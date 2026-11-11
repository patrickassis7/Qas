package br.com.organizacaorecursos.report.application;

public class InvalidReportPeriodException extends RuntimeException {

    public InvalidReportPeriodException(String message) {
        super(message);
    }
}