package br.com.organizacaorecursos.audit.application;

import br.com.organizacaorecursos.audit.domain.AuditEventEntity;
import br.com.organizacaorecursos.audit.infrastructure.AuditEventRepository;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuditService {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "password_hash", "token", "secret", "authorization"
    );

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditEventRepository,
                        UserRepository userRepository,
                        ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditEventEntity registrar(UUID ator, String acao, String entidade, UUID id,
                                      Map<String, ?> detalhes) {
        JsonNode detailsNode = objectMapper.valueToTree(detalhes == null ? Map.of() : detalhes);
        validateDetails(detailsNode);

        UserEntity actor = ator == null
                ? null
                : userRepository.findById(ator)
                .orElseThrow(() -> new IllegalArgumentException("Ator da auditoria não encontrado"));

        AuditEventEntity event = new AuditEventEntity(
                actor,
                acao,
                entidade,
                id,
                Instant.now(),
                detailsNode
        );
        return auditEventRepository.save(event);
    }

    private void validateDetails(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Detalhes da auditoria devem ser um objeto JSON");
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String normalizedKey = field.getKey().toLowerCase(Locale.ROOT);
            if (SENSITIVE_KEYS.stream().anyMatch(normalizedKey::contains)) {
                throw new IllegalArgumentException("Detalhes da auditoria não podem conter dados sensíveis");
            }
            validateNested(field.getValue());
        }
    }

    private void validateNested(JsonNode node) {
        if (node.isObject()) {
            validateDetails(node);
        } else if (node.isArray()) {
            node.forEach(this::validateNested);
        }
    }
}
