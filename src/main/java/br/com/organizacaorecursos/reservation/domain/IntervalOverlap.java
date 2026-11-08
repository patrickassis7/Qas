package br.com.organizacaorecursos.reservation.domain;

import java.time.Instant;
import java.util.Objects;

public final class IntervalOverlap {

    private IntervalOverlap() {
    }

    /** Intervalos semiabertos [a, b) e [c, d): sobrepõem-se quando a < d && c < b. */
    public static boolean overlaps(Instant a, Instant b, Instant c, Instant d) {
        requireValid(a, b);
        requireValid(c, d);
        return a.isBefore(d) && c.isBefore(b);
    }

    private static void requireValid(Instant start, Instant end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Fim deve ser posterior ao início");
        }
    }
}
