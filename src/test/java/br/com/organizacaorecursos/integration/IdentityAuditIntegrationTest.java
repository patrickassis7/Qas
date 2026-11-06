package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.audit.application.AuditService;
import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import br.com.organizacaorecursos.audit.infrastructure.AuditEventRepository;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class IdentityAuditIntegrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditService auditService;

    @Test
    @Transactional
    void migrationCreatesIdentityAndAuditTablesAndSeedsRoles() {
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("roles")).isTrue();
        assertThat(tableExists("user_roles")).isTrue();
        assertThat(tableExists("audit_events")).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class)).isEqualTo(3);
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void foreignKeysAndEmailUniquenessAreEnforced() {
        UserEntity user = userRepository.saveAndFlush(
                new UserEntity("Usuário de teste", "unique@example.test", "hash-ficticio", true)
        );

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                new UserEntity("Outro usuário", "unique@example.test", "outro-hash", true)
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?)",
                UUID.randomUUID(), UUID.randomUUID()
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(user.getId()).isNotNull();
    }

    @Test
    @Transactional
    void persistsHumanAndSystemAuditEvents() {
        UserEntity user = userRepository.saveAndFlush(
                new UserEntity("Ator de teste", "actor@example.test", "hash-ficticio", true)
        );

        AuditEventEntity humanEvent = auditService.registrar(
                user.getId(), "USER_CREATED", "USER", user.getId(), Map.of("source", "integration-test")
        );
        AuditEventEntity systemEvent = auditService.registrar(
                null, "SYSTEM_CHECK", "SYSTEM", null, Map.of("source", "integration-test")
        );

        assertThat(humanEvent.getId()).isNotNull();
        assertThat(humanEvent.getActor().getId()).isEqualTo(user.getId());
        assertThat(systemEvent.getActor()).isNull();
        assertThat(auditEventRepository.count()).isEqualTo(2);
    }

    @Test
    void rejectsSensitiveAuditDetails() {
        assertThatThrownBy(() -> auditService.registrar(
                null, "UNSAFE", "SYSTEM", null, Map.of("password", "not-allowed")
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO audit_events(action, entity_type, details) "
                        + "VALUES (?, ?, ?::jsonb)",
                "UNSAFE_DIRECT", "SYSTEM", "{\"nested\": {\"token\": \"not-allowed\"}}"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = ?)",
                Boolean.class,
                tableName
        ));
    }
}
