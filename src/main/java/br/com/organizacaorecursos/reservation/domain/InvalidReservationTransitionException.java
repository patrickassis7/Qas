package br.com.organizacaorecursos.reservation.domain;

public class InvalidReservationTransitionException extends RuntimeException {

    public InvalidReservationTransitionException(ReservationStatus from, ReservationStatus to) {
        super("Transição de estado inválida: " + from + " -> " + to);
    }
}
