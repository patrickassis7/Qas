package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationConcurrencyIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";
    private static final int THREADS = 8;
    private static final Instant START = Instant.parse("2030-05-06T08:00:00Z");
    private static final Instant END = Instant.parse("2030-05-06T10:00:00Z");
    private static final List<String> TOKENS = new ArrayList<>();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired ResourceRepository resources;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void tokens() throws Exception {
        if (!TOKENS.isEmpty()) {
            return;
        }
        for (int i = 0; i < THREADS; i++) {
            UserEntity user = new UserEntity("Fixture", UUID.randomUUID() + "@example.test",
                    passwords.encode(PASSWORD), true);
            user.addRole(roles.findByName("SOLICITANTE").orElseThrow());
            users.saveAndFlush(user);
            String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(
                                    Map.of("email", user.getEmail(), "password", PASSWORD))))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            TOKENS.add(mapper.readTree(response).get("accessToken").asText());
        }
    }

    @RepeatedTest(20)
    void onlyOneOfManyConcurrentRequestsForSameRoomAndTimeIsAccepted() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA);

        List<Integer> statuses = fire(i -> List.of(room));

        assertThat(statuses).filteredOn(code -> code == 201).hasSize(1);
        assertThat(statuses).filteredOn(code -> code == 409).hasSize(THREADS - 1);
        assertThat(reservationRowsFor(room)).isEqualTo(1);
        assertThat(conflictEventsFor(room)).isEqualTo(THREADS - 1);
        assertThat(orphanReservations()).isZero();
    }

    @RepeatedTest(20)
    void overlappingMultiResourceRequestsInCrossedOrderAcceptExactlyOneWithoutDeadlock() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA);
        ResourceEntity teacher = resource(ResourceType.PROFESSOR);
        ResourceEntity material = resource(ResourceType.MATERIAL);
        List<List<ResourceEntity>> combos = List.of(
                List.of(room, teacher), List.of(teacher, material), List.of(material, room));

        List<Integer> statuses = fire(i -> combos.get(i % combos.size()));

        assertThat(statuses).filteredOn(code -> code == 201).hasSize(1);
        assertThat(statuses).filteredOn(code -> code == 409).hasSize(THREADS - 1);
        long linked = reservationRowsFor(room) + reservationRowsFor(teacher) + reservationRowsFor(material);
        assertThat(linked).isEqualTo(2);
        assertThat(orphanReservations()).isZero();
    }

    private List<Integer> fire(java.util.function.IntFunction<List<ResourceEntity>> targets) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                String token = TOKENS.get(i);
                List<UUID> ids = targets.apply(i).stream().map(ResourceEntity::getId).toList();
                Callable<Integer> call = () -> {
                    String body = mapper.writeValueAsString(Map.of(
                            "startAt", START.toString(), "endAt", END.toString(), "resourceIds", ids));
                    ready.countDown();
                    go.await();
                    return mvc.perform(post("/api/v1/reservations")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON).content(body))
                            .andReturn().getResponse().getStatus();
                };
                futures.add(pool.submit(call));
            }
            assertThat(ready.await(30, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statuses.add(future.get(60, TimeUnit.SECONDS));
            }
            return statuses;
        } finally {
            pool.shutdownNow();
        }
    }

    private ResourceEntity resource(ResourceType type) {
        return resources.saveAndFlush(new ResourceEntity(
                type, "Recurso " + UUID.randomUUID(), "Bloco A", null, null, false, null));
    }

    private int reservationRowsFor(ResourceEntity resource) {
        return jdbc.queryForObject(
                "select count(*) from reservation_resources where resource_id = ?", Integer.class, resource.getId());
    }

    private int conflictEventsFor(ResourceEntity resource) {
        return jdbc.queryForObject(
                "select count(*) from audit_events where action = 'CONFLITO_EVITADO' and details::text like ?",
                Integer.class, "%" + resource.getId() + "%");
    }

    private int orphanReservations() {
        return jdbc.queryForObject("""
                select count(*) from reservations r
                where not exists (select 1 from reservation_resources rr where rr.reservation_id = r.id)
                """, Integer.class);
    }
}
