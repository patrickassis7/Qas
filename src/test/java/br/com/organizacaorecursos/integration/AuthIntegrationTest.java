package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthIntegrationTest.TestRoutes.class)
class AuthIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-password-only-for-tests";
    private static final String ISSUER = "organizacao-recursos";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    private static UserEntity solicitante;
    private static UserEntity responsavel;
    private static UserEntity administrador;
    private static UserEntity inativo;

    @BeforeEach
    void fixtures() {
        if (solicitante != null) {
            return;
        }
        solicitante = create("SOLICITANTE", true);
        responsavel = create("RESPONSAVEL", true);
        administrador = create("ADMINISTRADOR", true);
        inativo = create("SOLICITANTE", false);
    }

    private UserEntity create(String role, boolean active) {
        UserEntity user = new UserEntity("Fixture", UUID.randomUUID() + "@example.test",
                passwords.encode(PASSWORD), active);
        user.addRole(roles.findByName(role).orElseThrow());
        return users.saveAndFlush(user);
    }

    private String login(UserEntity user) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", user.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void validLoginReturnsFunctionalTokenAndNoHash() throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", solicitante.getEmail(), "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("passwordHash", "password_hash", PASSWORD);
        String token = mapper.readTree(response).get("accessToken").asText();
        mvc.perform(get("/api/v1/test/authenticated").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedApiResponsesIncludeDefaultSecurityHeaders() throws Exception {
        mvc.perform(get("/api/v1/test/authenticated").header("Authorization", "Bearer " + login(solicitante)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @ParameterizedTest
    @CsvSource({"wrong, 401", "unknown, 401", "inactive, 401"})
    void invalidLoginsDoNotDiscloseAccountState(String kind, int expected) throws Exception {
        String email = kind.equals("unknown") ? "absent@example.test"
                : kind.equals("inactive") ? inativo.getEmail() : solicitante.getEmail();
        String password = kind.equals("wrong") ? "invalid-synthetic-password" : PASSWORD;
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().is(expected))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void invalidBodyHasUniformErrorWithoutEchoingPassword() throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("password_hash", "passwordHash", "stackTrace");
    }

    @ParameterizedTest
    @CsvSource({"absent", "malformed", "expired", "wrong-signature", "wrong-issuer", "too-long"})
    void missingOrInvalidTokensAreRejected(String kind) throws Exception {
        var request = get("/api/v1/test/authenticated");
        if (!kind.equals("absent")) {
            String token = switch (kind) {
                case "expired" -> signed(solicitante.getId(), Instant.now().minusSeconds(3600),
                        Instant.now().minusSeconds(1800), encoder);
                case "wrong-signature" -> signed(solicitante.getId(), Instant.now(),
                        Instant.now().plusSeconds(600), new NimbusJwtEncoder(new ImmutableSecret<>(
                                new SecretKeySpec("different-synthetic-test-only-signing-key-2026".getBytes(), "HmacSHA256"))));
                case "wrong-issuer" -> signedWithIssuer(solicitante.getId(), Instant.now(),
                        Instant.now().plusSeconds(600), encoder, "untrusted-issuer");
                case "too-long" -> signed(solicitante.getId(), Instant.now(),
                        Instant.now().plusSeconds(7200), encoder);
                default -> "not-a-jwt";
            };
            request.header("Authorization", "Bearer " + token);
        }
        mvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Autenticação necessária ou inválida."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void missingProtectedRouteWithValidTokenReturnsUniformNotFound() throws Exception {
        String token = login(solicitante);
        String response = mvc.perform(get("/api/v1/test/route-that-does-not-exist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("A rota solicitada não foi encontrada."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("NoResourceFoundException", "stackTrace", "exception");
    }

    @Test
    void unexpectedServerExceptionRemainsGeneric500() throws Exception {
        String response = mvc.perform(get("/api/v1/test/server-error")
                        .header("Authorization", "Bearer " + login(solicitante)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Não foi possível processar a solicitação."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("synthetic server failure", "stackTrace", "exception");
    }

    @ParameterizedTest
    @CsvSource({"SOLICITANTE, solicitante, 200", "SOLICITANTE, administrador, 403",
            "RESPONSAVEL, responsavel, 200", "RESPONSAVEL, solicitante, 403",
            "ADMINISTRADOR, administrador, 200", "ADMINISTRADOR, responsavel, 403"})
    void authorizesByCurrentRole(String role, String route, int expected) throws Exception {
        UserEntity user = switch (role) {
            case "SOLICITANTE" -> solicitante;
            case "RESPONSAVEL" -> responsavel;
            default -> administrador;
        };
        var result = mvc.perform(get("/api/v1/test/" + route)
                        .header("Authorization", "Bearer " + login(user)))
                .andExpect(status().is(expected));
        if (expected == 403) {
            result.andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Acesso não permitido para seu perfil."))
                    .andExpect(jsonPath("$.fieldErrors").isArray())
                    .andExpect(jsonPath("$.traceId").isNotEmpty());
        }
    }

    @Test
    void deactivatedUserCannotUsePreviouslyIssuedToken() throws Exception {
        UserEntity user = create("SOLICITANTE", true);
        String token = login(user);
        jdbc.update("UPDATE users SET active = false WHERE id = ?", user.getId());
        mvc.perform(get("/api/v1/test/authenticated").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void healthAndDocumentationRemainPublic() throws Exception {
        for (String path : new String[]{"/actuator/health", "/v3/api-docs", "/swagger-ui/index.html"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension.class)
    void credentialsAndTokenAreNotLogged(CapturedOutput output) throws Exception {
        String token = login(solicitante);
        mvc.perform(get("/api/v1/test/authenticated").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        String rejected = "long-rejected-synthetic-password-" + "X".repeat(160);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", solicitante.getEmail(), "password", rejected))))
                .andExpect(status().isBadRequest());
        assertThat(output.getAll()).doesNotContain(PASSWORD, token,
                rejected, "synthetic-test-only-hs256-signing-key-2026-11-06-long");
    }

    private String signed(UUID subject, Instant issuedAt, Instant expiresAt, JwtEncoder signer) {
        return signedWithIssuer(subject, issuedAt, expiresAt, signer, ISSUER);
    }

    private String signedWithIssuer(UUID subject, Instant issuedAt, Instant expiresAt,
                                    JwtEncoder signer, String issuer) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject(subject.toString())
                .issuedAt(issuedAt).expiresAt(expiresAt).build();
        return signer.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @RestController
    static class TestRoutes {
        @GetMapping("/api/v1/test/authenticated")
        String authenticated() { return "ok"; }

        @GetMapping("/api/v1/test/solicitante")
        @PreAuthorize("hasRole('SOLICITANTE')")
        String solicitante() { return "ok"; }

        @GetMapping("/api/v1/test/responsavel")
        @PreAuthorize("hasRole('RESPONSAVEL')")
        String responsavel() { return "ok"; }

        @GetMapping("/api/v1/test/administrador")
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        String administrador() { return "ok"; }

        @GetMapping("/api/v1/test/server-error")
        String serverError() { throw new IllegalStateException("synthetic server failure"); }
    }
}
