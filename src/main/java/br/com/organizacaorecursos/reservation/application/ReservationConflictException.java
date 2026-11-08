package br.com.organizacaorecursos.reservation.application;

import java.util.List;

public class ReservationConflictException extends RuntimeException {

    private final List<String> conflicts;

    public ReservationConflictException(List<String> conflicts) {
        super("Conflito de agenda no período informado: " + String.join("; ", conflicts) + ".");
        this.conflicts = List.copyOf(conflicts);
    }

    public List<String> getConflicts() {
        return conflicts;
    }
}
