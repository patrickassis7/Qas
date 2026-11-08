package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApprovalIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";
    private static final Instant FUTURE = Instant.parse("2030-03-04T00:00:00Z");
    private static final Instant PAST = Instant.parse("2020-03-04T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired ResourceRepository resources;
    @Autowired JdbcTemplate jdbc;

    private UserEntity requester;
    private UserEntity responsible;
    private UserEntity bystander;
    private UserEntity admin;
    private String requesterToken;
    private String responsibleToken;
    private String bystanderToken;
    private String adminToken;
    private ResourceEntity restricted;

    @BeforeEach
    void fixtures() throws Exception {
        requester = user("SOLICITANTE");
        responsible = user("RESPONSAVEL");
        bystander = user("SOLICITANTE");
        admin = user("ADMINISTRADOR");
        requesterToken = login(requester);
        responsibleToken = login(responsible);
        bystanderToken = login(bystander);
        adminToken = login(admin);
        restricted = resources.saveAndFlush(new ResourceEntity(
                ResourceType.MATERIAL, "Restrito " + UUID.randomUUID(), "Lab", null, null, true, responsible));
    }

    @Test
    @DisplayName("Dado um recurso restrito, quando o solicitante reserva, então fica SOLICITADA sem aprovação")
    void restrictedReservationWaitsForDecision() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);

        assertThat(statusOf(id)).isEqualTo("SOLICITADA");
        assertThat(count("approvals", id)).isZero();
        assertThat(auditCount("RESERVA_APROVADA_AUTOMATICAMENTE", id)).isZero();
    }

    @Test
    @DisplayName("Dada uma reserva SOLICITADA, quando o responsável aprova, então APROVADA com decisão e auditoria")
    void responsibleApproves() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);

        action(responsibleToken, id, "approve").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        assertThat(statusOf(id)).isEqualTo("APROVADA");
        assertThat(count("approvals", id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select decided_by from approvals where reservation_id = ?::uuid",
                UUID.class, id)).isEqualTo(responsible.getId());
        assertThat(auditCount("RESERVA_APROVADA", id)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dada uma reserva SOLICITADA, quando o administrador rejeita, então REJEITADA e a agenda é liberada")
    void adminRejectsAndFreesAgenda() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        createRequest(bystanderToken, FUTURE, 9, 11).andExpect(status().isConflict());

        action(adminToken, id, "reject", "Sem justificativa").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));

        assertThat(auditCount("RESERVA_REJEITADA", id)).isEqualTo(1);
        createRequest(bystanderToken, FUTURE, 9, 11).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Dada uma reserva SOLICITADA, quando o solicitante tenta aprovar a própria, então 403")
    void selfApprovalIsDenied() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);

        action(requesterToken, id, "approve").andExpect(status().isForbidden());
        action(requesterToken, id, "reject").andExpect(status().isForbidden());

        assertThat(statusOf(id)).isEqualTo("SOLICITADA");
        assertThat(count("approvals", id)).isZero();
    }

    @Test
    @DisplayName("Dado um responsável que também solicita, quando aprova a própria reserva, então 403")
    void responsibleCannotApproveOwnRequest() throws Exception {
        String id = id(createRequest(responsibleToken, FUTURE, 8, 10).andExpect(status().isCreated()));

        action(responsibleToken, id, "approve").andExpect(status().isForbidden());
        action(adminToken, id, "approve").andExpect(status().isOk());
    }

    @Test
    @DisplayName("Dado um usuário sem relação com o recurso, quando decide, então 403")
    void bystanderCannotDecideOrOperate() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);

        action(bystanderToken, id, "approve").andExpect(status().isForbidden());
        action(responsibleToken, id, "approve").andExpect(status().isOk());
        action(bystanderToken, id, "start").andExpect(status().isForbidden());
        action(requesterToken, id, "start").andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dada uma reserva já decidida, quando outra decisão chega, então 409 e uma única decisão gravada")
    void duplicateDecisionIsConflict() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        action(responsibleToken, id, "approve").andExpect(status().isOk());

        action(responsibleToken, id, "approve").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TRANSITION"));
        action(adminToken, id, "reject").andExpect(status().isConflict());

        assertThat(statusOf(id)).isEqualTo("APROVADA");
        assertThat(count("approvals", id)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dada uma reserva APROVADA, quando inicia e conclui, então registra retirada e devolução com auditoria")
    void startAndCompleteRecordWithdrawalAndReturn() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        action(responsibleToken, id, "approve").andExpect(status().isOk());

        action(responsibleToken, id, "start", "Retirado no balcão").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_USO"));
        assertThat(jdbc.queryForObject(
                "select returned_at is null from withdrawal_records where reservation_id = ?::uuid",
                Boolean.class, id)).isTrue();

        action(responsibleToken, id, "complete").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
        assertThat(jdbc.queryForObject(
                "select returned_by from withdrawal_records where reservation_id = ?::uuid",
                UUID.class, id)).isEqualTo(responsible.getId());

        assertThat(auditCount("RESERVA_CRIADA", id)).isEqualTo(1);
        assertThat(auditCount("RESERVA_APROVADA", id)).isEqualTo(1);
        assertThat(auditCount("RESERVA_INICIADA", id)).isEqualTo(1);
        assertThat(auditCount("RESERVA_CONCLUIDA", id)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dada uma reserva sem retirada, quando tenta devolver, então 409 sem registro de devolução")
    void returnWithoutWithdrawalIsDenied() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        action(responsibleToken, id, "complete").andExpect(status().isConflict());
        action(responsibleToken, id, "approve").andExpect(status().isOk());

        action(responsibleToken, id, "complete").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_ALLOWED"));

        assertThat(statusOf(id)).isEqualTo("APROVADA");
        assertThat(count("withdrawal_records", id)).isZero();
        assertThat(auditCount("RESERVA_CONCLUIDA", id)).isZero();
    }

    @Test
    @DisplayName("Dada uma reserva EM_USO ou CONCLUIDA, quando tenta cancelar, então 409 e o estado permanece")
    void inUseAndCompletedCannotBeCancelled() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        action(responsibleToken, id, "approve").andExpect(status().isOk());
        action(responsibleToken, id, "start").andExpect(status().isOk());

        action(requesterToken, id, "cancel").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TRANSITION"));
        assertThat(statusOf(id)).isEqualTo("EM_USO");

        action(responsibleToken, id, "complete").andExpect(status().isOk());
        action(adminToken, id, "cancel").andExpect(status().isConflict());
        assertThat(statusOf(id)).isEqualTo("CONCLUIDA");
        assertThat(auditCount("RESERVA_CANCELADA", id)).isZero();
    }

    @Test
    @DisplayName("Dada uma reserva APROVADA no futuro, quando registra não comparecimento, então 409")
    void noShowBeforeStartIsDenied() throws Exception {
        String id = requestReservation(FUTURE, 8, 10);
        action(responsibleToken, id, "approve").andExpect(status().isOk());

        action(responsibleToken, id, "no-show").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_ALLOWED"));

        assertThat(statusOf(id)).isEqualTo("APROVADA");
    }

    @Test
    @DisplayName("Dada uma reserva APROVADA cujo horário já começou, quando registra não comparecimento, então NAO_COMPARECEU")
    void noShowAfterStartIsRecorded() throws Exception {
        String id = id(createRequest(requesterToken, PAST, 8, 10).andExpect(status().isCreated()));
        action(responsibleToken, id, "no-show").andExpect(status().isConflict());
        action(responsibleToken, id, "approve").andExpect(status().isOk());

        action(responsibleToken, id, "no-show").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_COMPARECEU"));

        assertThat(auditCount("RESERVA_NAO_COMPARECEU", id)).isEqualTo(1);
        action(responsibleToken, id, "start").andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Dada uma reserva SOLICITADA, quando outra pede o mesmo horário, então 409 conforme D1")
    void requestedReservationOccupiesAgenda() throws Exception {
        requestReservation(FUTURE, 8, 10);

        createRequest(bystanderToken, FUTURE, 9, 11).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"));
        createRequest(bystanderToken, FUTURE, 10, 12).andExpect(status().isCreated());
    }

    private String requestReservation(Instant day, int startHour, int endHour) throws Exception {
        return id(createRequest(requesterToken, day, startHour, endHour).andExpect(status().isCreated()));
    }

    private org.springframework.test.web.servlet.ResultActions createRequest(
            String token, Instant day, int startHour, int endHour) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("startAt", day.plusSeconds(startHour * 3600L).toString());
        body.put("endAt", day.plusSeconds(endHour * 3600L).toString());
        body.put("resourceIds", List.of(restricted.getId()));
        return mvc.perform(post("/api/v1/reservations").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)));
    }

    private org.springframework.test.web.servlet.ResultActions action(String token, String id, String name)
            throws Exception {
        return mvc.perform(post("/api/v1/reservations/" + id + "/" + name)
                .header("Authorization", "Bearer " + token));
    }

    private org.springframework.test.web.servlet.ResultActions action(
            String token, String id, String name, String note) throws Exception {
        return mvc.perform(post("/api/v1/reservations/" + id + "/" + name)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("note", note))));
    }

    private String id(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        return mapper.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asText();
    }

    private String statusOf(String id) {
        return jdbc.queryForObject("select status from reservations where id = ?::uuid", String.class, id);
    }

    private int count(String table, String reservationId) {
        String column = "reservations".equals(table) ? "id" : "reservation_id";
        return jdbc.queryForObject(
                "select count(*) from " + table + " where " + column + " = ?::uuid", Integer.class, reservationId);
    }

    private int auditCount(String action, String reservationId) {
        return jdbc.queryForObject(
                "select count(*) from audit_events where action = ? and entity_id = ?::uuid",
                Integer.class, action, reservationId);
    }

    private UserEntity user(String role) {
        UserEntity user = new UserEntity("Fixture", UUID.randomUUID() + "@example.test",
                passwords.encode(PASSWORD), true);
        user.addRole(roles.findByName(role).orElseThrow());
        return users.saveAndFlush(user);
    }

    private String login(UserEntity user) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("accessToken").asText();
    }
}
