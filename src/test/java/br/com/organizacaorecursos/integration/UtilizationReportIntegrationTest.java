package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import br.com.organizacaorecursos.audit.infrastructure.AuditEventRepository;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import br.com.organizacaorecursos.reservation.infrastructure.ReservationRepository;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UtilizationReportIntegrationTest extends PostgresIntegrationTestSupport {

    private static final Instant START = Instant.parse("2035-05-10T08:00:00Z");
    private static final Instant END = Instant.parse("2035-05-10T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository users;
    @Autowired ResourceRepository resources;
    @Autowired ReservationRepository reservations;
    @Autowired AuditEventRepository auditEvents;

    @Test
    void reportsClippedAllocatedHoursUtilizationAndAvoidedConflictsByResource() throws Exception {
        UserEntity requester = users.saveAndFlush(new UserEntity(
                "Solicitante de relatório", "report-" + UUID.randomUUID() + "@example.test", "hash-ficticio", true));
        ResourceEntity room = resource("Sala relatório");
        ResourceEntity material = resource("Material relatório");

        reservation(requester, room, START.minusSeconds(3600), START.plusSeconds(7200), ReservationStatus.APROVADA);
        reservation(requester, room, START.plusSeconds(10800), END.plusSeconds(3600), ReservationStatus.CONCLUIDA);
        reservation(requester, material, START.plusSeconds(3600), START.plusSeconds(7200), ReservationStatus.APROVADA);
        reservation(requester, room, START, END, ReservationStatus.CANCELADA);
        reservation(requester, material, START, END, ReservationStatus.REJEITADA);

        conflict(room, START.plusSeconds(3600));
        conflict(room, START.plusSeconds(7200));
        conflict(material, START.plusSeconds(7200));

        JsonNode report = objectMapper.readTree(mvc.perform(get("/api/v1/reports/utilization")
                        .param("startAt", START.toString())
                        .param("endAt", END.toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RESPONSAVEL"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(report.get("resources")).hasSize(2);
        JsonNode roomResult = resourceResult(report, room.getId());
        JsonNode materialResult = resourceResult(report, material.getId());
        assertThat(roomResult.get("allocatedHours").decimalValue()).isEqualByComparingTo("3.00");
        assertThat(roomResult.get("utilizationPercent").decimalValue()).isEqualByComparingTo("75.00");
        assertThat(roomResult.get("avoidedConflicts").longValue()).isEqualTo(2);
        assertThat(materialResult.get("allocatedHours").decimalValue()).isEqualByComparingTo("1.00");
        assertThat(materialResult.get("utilizationPercent").decimalValue()).isEqualByComparingTo("25.00");
        assertThat(materialResult.get("avoidedConflicts").longValue()).isEqualTo(1);

        JsonNode filtered = objectMapper.readTree(mvc.perform(get("/api/v1/reports/utilization")
                        .param("startAt", START.toString())
                        .param("endAt", END.toString())
                        .param("resourceId", room.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(filtered.get("resources")).hasSize(1);
        assertThat(filtered.get("resources").get(0).get("resourceId").asText()).isEqualTo(room.getId().toString());

        mvc.perform(get("/api/v1/reports/utilization")
                        .param("startAt", START.toString())
                        .param("endAt", END.toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SOLICITANTE"))))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reports/utilization")
                        .param("startAt", END.toString())
                        .param("endAt", START.toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                .andExpect(status().isBadRequest());
    }

    private ResourceEntity resource(String name) {
        return resources.saveAndFlush(new ResourceEntity(
                ResourceType.SALA, name + " " + UUID.randomUUID(), "Bloco A", null, null, false, null));
    }

    private void reservation(
            UserEntity requester, ResourceEntity resource, Instant startAt, Instant endAt, ReservationStatus status
    ) {
        ReservationEntity reservation = new ReservationEntity(
                requester, startAt, endAt, "fixture", List.of(resource));
        if (status == ReservationStatus.REJEITADA) {
            reservation.transitionTo(ReservationStatus.REJEITADA);
            reservations.saveAndFlush(reservation);
            return;
        }
        reservation.transitionTo(ReservationStatus.APROVADA);
        if (status == ReservationStatus.EM_USO || status == ReservationStatus.CONCLUIDA) {
            reservation.transitionTo(ReservationStatus.EM_USO);
        }
        if (status == ReservationStatus.CONCLUIDA) {
            reservation.transitionTo(ReservationStatus.CONCLUIDA);
        } else if (status == ReservationStatus.CANCELADA || status == ReservationStatus.REJEITADA) {
            reservation.transitionTo(status);
        }
        reservations.saveAndFlush(reservation);
    }

    private void conflict(ResourceEntity resource, Instant occurredAt) {
        auditEvents.saveAndFlush(new AuditEventEntity(null, "CONFLITO_EVITADO", "RESERVATION", null,
                occurredAt, objectMapper.valueToTree(Map.of("resourceIds", List.of(resource.getId().toString())))));
    }

    private static JsonNode resourceResult(JsonNode report, UUID resourceId) {
        return StreamSupport.stream(report.get("resources").spliterator(), false)
                .filter(resource -> resource.get("resourceId").asText().equals(resourceId.toString()))
                .findFirst()
                .orElseThrow();
    }
}