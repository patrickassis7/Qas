package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PublicApiWorkflowEndToEndTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";

    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired JdbcTemplate jdbc;

    private UserEntity requester;
    private UserEntity responsible;
    private String requesterToken;
    private String responsibleToken;
    private String adminToken;

    @BeforeEach
    void createUsersAndAuthenticateOverHttp() throws Exception {
        requester = user("SOLICITANTE");
        responsible = user("RESPONSAVEL");
        UserEntity admin = user("ADMINISTRADOR");
        requesterToken = login(requester);
        responsibleToken = login(responsible);
        adminToken = login(admin);
    }

    @Test
    void restrictedResourceLifecyclePersistsStateAndAuditEventsThroughPublicHttp() throws Exception {
        ResponseEntity<String> resourceResponse = request(HttpMethod.POST, "/api/v1/resources", adminToken,
                Map.of("type", "MATERIAL", "name", "Microscópio restrito " + UUID.randomUUID(),
                        "location", "Laboratorio", "skills", "optics", "restricted", true,
                        "responsibleId", responsible.getId()));
        assertThat(resourceResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String resourceId = json(resourceResponse).path("id").asText();

        ResponseEntity<String> searchResponse = request(HttpMethod.GET,
                "/api/v1/resources?type=MATERIAL&location=LABORATORIO&skill=Optics&status=ATIVO",
                requesterToken, null);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(searchResponse).path("content").findValuesAsText("id")).contains(resourceId);

        Instant startAt = Instant.now().plusSeconds(30L * 24 * 60 * 60).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        ResponseEntity<String> reservationResponse = request(HttpMethod.POST, "/api/v1/reservations", requesterToken,
                Map.of("startAt", startAt.toString(), "endAt", startAt.plusSeconds(2 * 60 * 60).toString(),
                        "resourceIds", List.of(resourceId)));
        assertThat(reservationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode reservation = json(reservationResponse);
        String reservationId = reservation.path("id").asText();
        assertThat(reservation.path("status").asText()).isEqualTo("SOLICITADA");

        ResponseEntity<String> deniedApproval = request(HttpMethod.POST,
                "/api/v1/reservations/" + reservationId + "/approve", requesterToken, null);
        assertThat(deniedApproval.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(request(HttpMethod.POST, "/api/v1/reservations/" + reservationId + "/approve",
                responsibleToken, null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(request(HttpMethod.POST, "/api/v1/reservations/" + reservationId + "/start",
                responsibleToken, null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(request(HttpMethod.POST, "/api/v1/reservations/" + reservationId + "/complete",
                responsibleToken, null).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(jdbc.queryForObject("select status from reservations where id = ?::uuid",
                String.class, reservationId)).isEqualTo("CONCLUIDA");
        assertThat(jdbc.queryForObject("select returned_by from withdrawal_records where reservation_id = ?::uuid",
                UUID.class, reservationId)).isEqualTo(responsible.getId());

        ResponseEntity<String> auditResponse = request(HttpMethod.GET,
                "/api/v1/audit-events?entityId=" + reservationId + "&size=100", adminToken, null);
        assertThat(auditResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> actions = json(auditResponse).path("content").findValuesAsText("action");
        assertThat(actions).contains("RESERVA_CRIADA", "RESERVA_APROVADA", "RESERVA_INICIADA", "RESERVA_CONCLUIDA");

        assertThat(request(HttpMethod.GET, "/api/v1/audit-events?entityId=" + reservationId,
                requesterToken, null).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String login(UserEntity user) throws Exception {
        ResponseEntity<String> response = request(HttpMethod.POST, "/api/v1/auth/login", null,
                Map.of("email", user.getEmail(), "password", PASSWORD));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return json(response).path("accessToken").asText();
    }

    private UserEntity user(String role) {
        UserEntity user = new UserEntity("HTTP E2E", UUID.randomUUID() + "@example.test",
                passwords.encode(PASSWORD), true);
        user.addRole(roles.findByName(role).orElseThrow());
        return users.saveAndFlush(user);
    }

    private ResponseEntity<String> request(HttpMethod method, String path, String token, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        String content = body == null ? null : mapper.writeValueAsString(body);
        return http.exchange(path, method, new HttpEntity<>(content, headers), String.class);
    }

    private JsonNode json(ResponseEntity<String> response) throws Exception {
        return mapper.readTree(response.getBody());
    }
}