package br.com.organizacaorecursos.reservation.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static br.com.organizacaorecursos.reservation.domain.ReservationStatus.*;

public final class ReservationStateMachine {

    private static final Map<ReservationStatus, Set<ReservationStatus>> TRANSITIONS =
            new EnumMap<>(ReservationStatus.class);

    static {
        TRANSITIONS.put(SOLICITADA, EnumSet.of(APROVADA, REJEITADA, CANCELADA));
        TRANSITIONS.put(APROVADA, EnumSet.of(EM_USO, CANCELADA, NAO_COMPARECEU));
        TRANSITIONS.put(EM_USO, EnumSet.of(CONCLUIDA));
        TRANSITIONS.put(CONCLUIDA, EnumSet.noneOf(ReservationStatus.class));
        TRANSITIONS.put(REJEITADA, EnumSet.noneOf(ReservationStatus.class));
        TRANSITIONS.put(CANCELADA, EnumSet.noneOf(ReservationStatus.class));
        TRANSITIONS.put(NAO_COMPARECEU, EnumSet.noneOf(ReservationStatus.class));
    }

    private ReservationStateMachine() {
    }

    public static boolean canTransition(ReservationStatus from, ReservationStatus to) {
        return TRANSITIONS.get(from).contains(to);
    }

    public static void requireTransition(ReservationStatus from, ReservationStatus to) {
        if (!canTransition(from, to)) {
            throw new InvalidReservationTransitionException(from, to);
        }
    }
}
