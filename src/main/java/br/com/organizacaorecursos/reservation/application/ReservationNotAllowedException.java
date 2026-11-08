package br.com.organizacaorecursos.reservation.application;

public class ReservationNotAllowedException extends RuntimeException {

    public ReservationNotAllowedException(String message) {
        super(message);
    }
}
