package br.com.organizacaorecursos.reservation.application;

public class InvalidReservationPeriodException extends RuntimeException {

    public InvalidReservationPeriodException(String message) {
        super(message);
    }
}
