package br.com.organizacaorecursos.reservation;

import br.com.organizacaorecursos.reservation.domain.InvalidReservationTransitionException;
import br.com.organizacaorecursos.reservation.domain.ReservationStateMachine;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static br.com.organizacaorecursos.reservation.domain.ReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStatusTransitionTest {

    private static final Map<ReservationStatus, Set<ReservationStatus>> VALID = Map.of(
            SOLICITADA, Set.of(APROVADA, REJEITADA, CANCELADA),
            APROVADA, Set.of(EM_USO, CANCELADA, NAO_COMPARECEU),
            EM_USO, Set.of(CONCLUIDA),
            CONCLUIDA, Set.of(),
            REJEITADA, Set.of(),
            CANCELADA, Set.of(),
            NAO_COMPARECEU, Set.of()
    );

    private static Stream<Arguments> allPairs(boolean valid) {
        Stream.Builder<Arguments> b = Stream.builder();
        for (ReservationStatus from : values()) {
            for (ReservationStatus to : values()) {
                if (VALID.get(from).contains(to) == valid) {
                    b.add(Arguments.of(from, to));
                }
            }
        }
        return b.build();
    }

    static Stream<Arguments> validTransitions() {
        return allPairs(true);
    }

    static Stream<Arguments> invalidTransitions() {
        return allPairs(false);
    }

    @Test
    void hasExactlyTheSevenStates() {
        assertThat(values()).containsExactly(
                SOLICITADA, APROVADA, EM_USO, CONCLUIDA, REJEITADA, CANCELADA, NAO_COMPARECEU);
    }

    @ParameterizedTest(name = "{0} -> {1} permitida")
    @MethodSource("validTransitions")
    void allowsValidTransitions(ReservationStatus from, ReservationStatus to) {
        assertThat(ReservationStateMachine.canTransition(from, to)).isTrue();
        assertThatNoException().isThrownBy(() -> ReservationStateMachine.requireTransition(from, to));
    }

    @ParameterizedTest(name = "{0} -> {1} recusada")
    @MethodSource("invalidTransitions")
    void rejectsInvalidTransitions(ReservationStatus from, ReservationStatus to) {
        assertThat(ReservationStateMachine.canTransition(from, to)).isFalse();
        assertThatThrownBy(() -> ReservationStateMachine.requireTransition(from, to))
                .isInstanceOf(InvalidReservationTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"SOLICITADA", "APROVADA", "EM_USO"})
    void occupyingStatesBlockTheAgenda(ReservationStatus status) {
        assertThat(status.occupiesAgenda()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"CONCLUIDA", "REJEITADA", "CANCELADA", "NAO_COMPARECEU"})
    void finalStatesDoNotBlockTheAgenda(ReservationStatus status) {
        assertThat(status.occupiesAgenda()).isFalse();
    }
}
