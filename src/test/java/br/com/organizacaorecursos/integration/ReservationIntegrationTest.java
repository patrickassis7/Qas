package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockEntity;
import br.com.organizacaorecursos.maintenance.infrastructure.MaintenanceBlockRepository;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";
    private static final Instant BASE = Instant.parse("2030-03-04T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired ResourceRepository resources;
    @Autowired MaintenanceBlockRepository maintenanceBlocks;
    @Autowired JdbcTemplate jdbc;

    private UserEntity owner;
    private UserEntity other;
    private UserEntity admin;
    private String ownerToken;
    private String otherToken;
    private String adminToken;

    @BeforeEach
    void fixtures() throws Exception {
        owner = user("SOLICITANTE");
        other = user("SOLICITANTE");
        admin = user("ADMINISTRADOR");
        ownerToken = login(owner);
        otherToken = login(other);
        adminToken = login(admin);
    }

    @Test
    void createsReservationForCommonResourceAutoApprovedWithAudit() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);

        JsonNode created = create(ownerToken, 8, 10, room);

        assertThat(created.get("status").asText()).isEqualTo("APROVADA");
        assertThat(created.get("requesterId").asText()).isEqualTo(owner.getId().toString());
        assertThat(auditCount("RESERVA_APROVADA_AUTOMATICAMENTE", created.get("id").asText())).isEqualTo(1);
        assertThat(auditCount("RESERVA_CRIADA", created.get("id").asText())).isEqualTo(1);
    }

    @Test
    void restrictedResourceStaysRequested() throws Exception {
        ResourceEntity restricted = resource(ResourceType.MATERIAL, true);

        JsonNode created = create(ownerToken, 8, 10, restricted);

        assertThat(created.get("status").asText()).isEqualTo("SOLICITADA");
        assertThat(auditCount("RESERVA_APROVADA_AUTOMATICAMENTE", created.get("id").asText())).isZero();
    }

    @Test
    void roomConflictReturns409AndRecordsAvoidedConflict() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        create(ownerToken, 8, 10, room);

        mvc.perform(reservationRequest(post("/api/v1/reservations"), otherToken, 9, 11, room))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(room.getName())));

        assertThat(conflictAuditCount(room)).isEqualTo(1);
        assertThat(reservationCount(room)).isEqualTo(1);
    }

    @Test
    void teacherConflictReturns409() throws Exception {
        ResourceEntity teacher = resource(ResourceType.PROFESSOR, false);
        ResourceEntity room = resource(ResourceType.SALA, false);
        create(ownerToken, 8, 10, teacher);

        mvc.perform(reservationRequest(post("/api/v1/reservations"), otherToken, 8, 10, room, teacher))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(teacher.getName())));

        assertThat(reservationCount(room)).isZero();
        String conflictingResourceIds = jdbc.queryForObject(
                "select details->'resourceIds'::text from audit_events "
                        + "where action = 'CONFLITO_EVITADO' order by occurred_at desc limit 1",
                String.class);
        assertThat(conflictingResourceIds).contains(teacher.getId().toString()).doesNotContain(room.getId().toString());
    }

    @Test
    void materialConflictReturns409() throws Exception {
        ResourceEntity material = resource(ResourceType.MATERIAL, false);
        create(ownerToken, 14, 16, material);

        mvc.perform(reservationRequest(post("/api/v1/reservations"), otherToken, 15, 17, material))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"));
    }

    @Test
    void adjacentIntervalsAreAllowed() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        create(ownerToken, 8, 10, room);

        create(otherToken, 10, 12, room);
        create(otherToken, 6, 8, room);

        assertThat(reservationCount(room)).isEqualTo(3);
    }

    @Test
    void maintenanceBlockPreventsReservation() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        maintenanceBlocks.saveAndFlush(new MaintenanceBlockEntity(
                room, at(9), at(11), "Reparo", admin));

        mvc.perform(reservationRequest(post("/api/v1/reservations"), ownerToken, 10, 12, room))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("manutenção")));

        create(ownerToken, 11, 13, room);
        assertThat(conflictAuditCount(room)).isEqualTo(1);
    }

    @Test
    void cancelledMaintenanceDoesNotBlock() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        MaintenanceBlockEntity block = new MaintenanceBlockEntity(
                room, at(8), at(12), "Reparo", admin);
        block.cancel();
        maintenanceBlocks.saveAndFlush(block);

        create(ownerToken, 8, 10, room);
    }

    @Test
    void onlyOwnerOrAdminCanReadChangeOrCancel() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        String id = create(ownerToken, 8, 10, room).get("id").asText();

        mvc.perform(get("/api/v1/reservations/" + id).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/reservations/" + id).header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"justification\":\"x\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/reservations/" + id + "/cancel")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reservations/" + id).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/reservations/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void listShowsOnlyOwnReservationsForNonAdmin() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        String id = create(ownerToken, 8, 10, room).get("id").asText();

        String otherList = mvc.perform(get("/api/v1/reservations").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String ownerList = mvc.perform(get("/api/v1/reservations").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(otherList).doesNotContain(id);
        assertThat(ownerList).contains(id);
    }

    @ParameterizedTest
    @CsvSource({
            "/api/v1/resources, -1, 20",
            "/api/v1/resources, 0, 0",
            "/api/v1/resources, 0, 101",
            "/api/v1/reservations, -1, 20",
            "/api/v1/reservations, 0, 0",
            "/api/v1/reservations, 0, 101"
    })
    void invalidPaginationReturnsUniformBadRequest(String path, int page, int size) throws Exception {
        String response = mvc.perform(get(path + "?page=" + page + "&size=" + size)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("IllegalArgumentException", "stackTrace", "exception");
    }

    @Test
    void malformedPaginationParameterReturnsUniformBadRequest() throws Exception {
        String response = mvc.perform(get("/api/v1/resources?page=not-a-number&size=20")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("page"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("not-a-number", "MethodArgumentTypeMismatchException", "stackTrace");
    }

    @Test
    void updateIgnoresItselfButConflictsWithOthers() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        String id = create(ownerToken, 8, 10, room).get("id").asText();
        create(otherToken, 12, 14, room);

        mvc.perform(reservationRequest(patch("/api/v1/reservations/" + id), ownerToken, 9, 11, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endAt").value(at(11).toString()));

        mvc.perform(reservationRequest(patch("/api/v1/reservations/" + id), ownerToken, 11, 13, null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"));

        mvc.perform(get("/api/v1/reservations/" + id).header("Authorization", "Bearer " + ownerToken))
                .andExpect(jsonPath("$.endAt").value(at(11).toString()));
    }

    @Test
    void cancelKeepsRecordFreesAgendaAndRejectsSecondCancel() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        String id = create(ownerToken, 8, 10, room).get("id").asText();

        mvc.perform(post("/api/v1/reservations/" + id + "/cancel").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        assertThat(reservationCount(room)).isEqualTo(1);
        mvc.perform(get("/api/v1/reservations/" + id).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        create(otherToken, 8, 10, room);

        mvc.perform(post("/api/v1/reservations/" + id + "/cancel").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TRANSITION"));
        mvc.perform(patch("/api/v1/reservations/" + id).header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"justification\":\"x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_ALLOWED"));
    }

    @Test
    void deleteIsNotExposed() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        String id = create(ownerToken, 8, 10, room).get("id").asText();

        mvc.perform(delete("/api/v1/reservations/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());
        assertThat(reservationCount(room)).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPeriodAndUnknownOrInactiveResource() throws Exception {
        ResourceEntity room = resource(ResourceType.SALA, false);
        ResourceEntity inactive = resource(ResourceType.SALA, false);
        inactive.deactivate();
        resources.saveAndFlush(inactive);

        mvc.perform(reservationRequest(post("/api/v1/reservations"), ownerToken, 10, 10, room))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RESERVATION_PERIOD"));
        mvc.perform(reservationRequest(post("/api/v1/reservations"), ownerToken, 8, 10, inactive))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_ALLOWED"));
        mvc.perform(post("/api/v1/reservations").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(8, 10, List.of(UUID.randomUUID()))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/reservations").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(8, 10, List.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/reservations")).andExpect(status().isUnauthorized());
    }

    @Test
    void availabilityByPeriodExcludesReservedAndMaintainedResources() throws Exception {
        ResourceEntity reserved = resource(ResourceType.SALA, false);
        ResourceEntity maintained = resource(ResourceType.SALA, false);
        ResourceEntity free = resource(ResourceType.SALA, false);
        create(ownerToken, 8, 10, reserved);
        maintenanceBlocks.saveAndFlush(new MaintenanceBlockEntity(
                maintained, at(8), at(12), "Reparo", admin));

        String body = mvc.perform(get("/api/v1/resources/availability")
                        .param("startAt", at(9).toString())
                        .param("endAt", at(11).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(body).contains(free.getId().toString())
                .doesNotContain(reserved.getId().toString(), maintained.getId().toString());

        mvc.perform(get("/api/v1/resources/availability").param("startAt", BASE.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    private JsonNode create(String token, int startHour, int endHour, ResourceEntity... targets) throws Exception {
        MvcResult result = mvc.perform(reservationRequest(post("/api/v1/reservations"), token, startHour, endHour, targets))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder reservationRequest(
            MockHttpServletRequestBuilder builder, String token, int startHour, int endHour, ResourceEntity... targets
    ) throws Exception {
        List<UUID> ids = targets == null ? null : java.util.Arrays.stream(targets).map(ResourceEntity::getId).toList();
        return builder.header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(startHour, endHour, ids));
    }

    private String json(int startHour, int endHour, List<UUID> resourceIds) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("startAt", at(startHour).toString());
        body.put("endAt", at(endHour).toString());
        if (resourceIds != null) {
            body.put("resourceIds", resourceIds);
        }
        return mapper.writeValueAsString(body);
    }

    private static Instant at(int hour) {
        return BASE.plusSeconds(hour * 3600L);
    }

    private ResourceEntity resource(ResourceType type, boolean restricted) {
        return resources.saveAndFlush(new ResourceEntity(
                type, "Recurso " + UUID.randomUUID(), "Bloco A", null, null, restricted, null));
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

    private int auditCount(String action, String reservationId) {
        return jdbc.queryForObject(
                "select count(*) from audit_events where action = ? and entity_id = ?::uuid",
                Integer.class, action, reservationId);
    }

    private int conflictAuditCount(ResourceEntity resource) {
        return jdbc.queryForObject(
                "select count(*) from audit_events where action = 'CONFLITO_EVITADO' and details::text like ?",
                Integer.class, "%" + resource.getId() + "%");
    }

    private int reservationCount(ResourceEntity resource) {
        return jdbc.queryForObject(
                "select count(*) from reservation_resources where resource_id = ?",
                Integer.class, resource.getId());
    }
}
