package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceMaintenanceIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";
    private static final Instant BASE = Instant.parse("2030-03-04T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
        @Autowired JdbcTemplate jdbc;

    private UserEntity admin;
    private UserEntity responsible;
    private UserEntity requester;
    private String adminToken;
    private String responsibleToken;
    private String requesterToken;

    @BeforeEach
    void fixtures() throws Exception {
        admin = user("ADMINISTRADOR");
        responsible = user("RESPONSAVEL");
        requester = user("SOLICITANTE");
        adminToken = login(admin);
        responsibleToken = login(responsible);
        requesterToken = login(requester);
    }

    @Test
    void resourceCrudAndSearchApplyAllFilters() throws Exception {
        String roomId = createResource(ResourceType.SALA, "Sala de Física", "Campus Norte", 32,
                "Physics,Optics", false, null);
        assertThat(auditCount("RECURSO_CRIADO", roomId)).isEqualTo(1);
        createResource(ResourceType.SALA, "Sala de Química", "Campus Sul", 20,
                "Chemistry", false, null);
        createResource(ResourceType.MATERIAL, "Microscópio", "Campus Norte", null,
                "Physics", true, responsible.getId());

        mvc.perform(get("/api/v1/resources")
                        .param("type", "SALA")
                        .param("location", "nOrTe")
                        .param("minimumCapacity", "30")
                        .param("skill", "physics")
                        .param("status", "ATIVO")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(roomId));

        mvc.perform(get("/api/v1/resources")
                        .param("type", "SALA")
                        .param("minimumCapacity", "33")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        Map<String, Object> update = resourceBody(ResourceType.SALA, "Sala Física Atualizada",
                "Campus Norte", 36, "Physics,Optics", false, null);
        mvc.perform(patch("/api/v1/resources/" + roomId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala Física Atualizada"))
                .andExpect(jsonPath("$.capacity").value(36));
        assertThat(auditCount("RECURSO_ATUALIZADO", roomId)).isEqualTo(1);

        mvc.perform(post("/api/v1/resources/" + roomId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(auditCount("RECURSO_DESATIVADO", roomId)).isEqualTo(1);

        mvc.perform(get("/api/v1/resources")
                        .param("status", "INATIVO")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')].status".formatted(roomId))
                        .value(org.hamcrest.Matchers.contains("INATIVO")));

        mvc.perform(get("/api/v1/resources/" + roomId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void onlyAdministratorCanCreateUpdateOrDeactivateResources() throws Exception {
        String resourceId = createResource(ResourceType.SALA, "Sala protegida", "Campus Norte", 24,
                "Physics", false, null);
        int resourceCount = jdbc.queryForObject("select count(*) from resources", Integer.class);
        Map<String, Object> update = resourceBody(ResourceType.SALA, "Sala alterada indevidamente",
                "Campus Sul", 30, "Chemistry", false, null);
        Map<String, Object> create = resourceBody(ResourceType.SALA, "Recurso não autorizado",
                "Campus Sul", 20, "Chemistry", false, null);

        for (String token : new String[]{requesterToken, responsibleToken}) {
            assertForbidden(mvc.perform(post("/api/v1/resources")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(create))));
            assertForbidden(mvc.perform(patch("/api/v1/resources/" + resourceId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(update))));
            assertForbidden(mvc.perform(post("/api/v1/resources/" + resourceId + "/deactivate")
                    .header("Authorization", "Bearer " + token)));
        }

        mvc.perform(get("/api/v1/resources/" + resourceId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala protegida"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
        assertThat(jdbc.queryForObject("select count(*) from resources", Integer.class)).isEqualTo(resourceCount);
    }

    private static void assertForbidden(org.springframework.test.web.servlet.ResultActions action) throws Exception {
        action.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Acesso não permitido para seu perfil."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void availabilityAndMaintenanceRespectIntervalsAndCancellation() throws Exception {
        String resourceId = createResource(ResourceType.MATERIAL, "Projetor de teste",
                "Lab", null, null, false, null);

        mvc.perform(get("/api/v1/resources/availability")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(resourceId)).isNotEmpty());

        mvc.perform(get("/api/v1/resources/availability")
                        .param("startAt", at(8).toString())
                        .param("endAt", at(10).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/resources/availability")
                        .param("startAt", at(8).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        String firstBlock = createMaintenance(resourceId, at(8), at(10))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String firstBlockId = mapper.readTree(firstBlock).path("id").asText();
        assertThat(auditCount("MANUTENCAO_CRIADA", firstBlockId)).isEqualTo(1);

        createMaintenance(resourceId, at(10), at(12)).andExpect(status().isCreated());
        createMaintenance(resourceId, at(9), at(11)).andExpect(status().isConflict());

        mvc.perform(get("/api/v1/maintenance-blocks/resource/" + resourceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(post("/api/v1/maintenance-blocks/" + firstBlockId + "/cancel")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(auditCount("MANUTENCAO_CANCELADA", firstBlockId)).isEqualTo(1);

        createMaintenance(resourceId, at(8), at(10)).andExpect(status().isCreated());
        createMaintenance(resourceId, at(12), at(12)).andExpect(status().isBadRequest());
        createMaintenance(UUID.randomUUID().toString(), at(13), at(14)).andExpect(status().isNotFound());
    }

    private String createResource(ResourceType type, String name, String location, Integer capacity,
                                  String skills, boolean restricted, UUID responsibleId) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(resourceBody(
                                type, name, location, capacity, skills, restricted, responsibleId))))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    private Map<String, Object> resourceBody(ResourceType type, String name, String location, Integer capacity,
                                             String skills, boolean restricted, UUID responsibleId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", type);
        body.put("name", name);
        body.put("location", location);
        body.put("capacity", capacity);
        body.put("skills", skills);
        body.put("restricted", restricted);
        body.put("responsibleId", responsibleId);
        return body;
    }

    private org.springframework.test.web.servlet.ResultActions createMaintenance(
            String resourceId, Instant startAt, Instant endAt) throws Exception {
        return mvc.perform(post("/api/v1/maintenance-blocks")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "resourceId", resourceId,
                        "startAt", startAt.toString(),
                        "endAt", endAt.toString(),
                        "reason", "Manutenção preventiva"))));
    }

    private UserEntity user(String role) {
        UserEntity user = new UserEntity("Fixture", UUID.randomUUID() + "@example.test",
                passwords.encode(PASSWORD), true);
        user.addRole(roles.findByName(role).orElseThrow());
        return users.saveAndFlush(user);
    }

    private String login(UserEntity user) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("accessToken").asText();
    }

        private int auditCount(String action, String entityId) {
                return jdbc.queryForObject(
                                "select count(*) from audit_events where action = ? and entity_id = ?::uuid",
                                Integer.class, action, entityId);
        }

    private static Instant at(int hour) {
        return BASE.plusSeconds(hour * 3600L);
    }
}
