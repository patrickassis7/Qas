package br.com.organizacaorecursos.reservation;

import br.com.organizacaorecursos.reservation.domain.IntervalOverlap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervalOverlapTest {

    private static Instant h(int hour) {
        return Instant.parse("2026-01-01T00:00:00Z").plusSeconds(hour * 3600L);
    }

    @ParameterizedTest(name = "[{0},{1}) x [{2},{3}) -> {4}")
    @CsvSource({
            "8, 10, 8, 10, true",    // idênticos
            "8, 10, 9, 11, true",    // parcial no fim
            "9, 11, 8, 10, true",    // parcial no início
            "8, 12, 9, 10, true",    // segundo contido no primeiro
            "9, 10, 8, 12, true",    // primeiro contido no segundo
            "8, 10, 10, 12, false",  // adjacente (b == c)
            "10, 12, 8, 10, false",  // adjacente (d == a)
            "8, 9, 10, 11, false",   // disjunto antes
            "10, 11, 8, 9, false"    // disjunto depois
    })
    void detectsOverlapOnHalfOpenIntervals(int a, int b, int c, int d, boolean expected) {
        assertThat(IntervalOverlap.overlaps(h(a), h(b), h(c), h(d))).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 8, 9, 11",  // primeiro invertido
            "9, 11, 10, 8",  // segundo invertido
            "8, 8, 9, 11",   // primeiro vazio
            "9, 11, 8, 8"    // segundo vazio
    })
    void rejectsInvertedOrEmptyIntervals(int a, int b, int c, int d) {
        assertThatThrownBy(() -> IntervalOverlap.overlaps(h(a), h(b), h(c), h(d)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNulls() {
        assertThatThrownBy(() -> IntervalOverlap.overlaps(null, h(1), h(0), h(2)))
                .isInstanceOf(NullPointerException.class);
    }
}
