package br.com.organizacaorecursos.reservation.domain;

public enum ReservationStatus {
    SOLICITADA,
    APROVADA,
    EM_USO,
    CONCLUIDA,
    REJEITADA,
    CANCELADA,
    NAO_COMPARECEU;

    /** Estados que ocupam a agenda do recurso (ADR D1). */
    public boolean occupiesAgenda() {
        return this == SOLICITADA || this == APROVADA || this == EM_USO;
    }
}
