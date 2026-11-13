package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.application.LocalIdentitySeeder;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class LocalSeedPasswordIntegrationTest extends PostgresIntegrationTestSupport {

    private static final String PASSWORD = "synthetic-local-seed-password";

    @DynamicPropertySource
    static void localPassword(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry, LocalSeedPasswordIntegrationTest.class);
        registry.add("LOCAL_SEED_PASSWORD", () -> PASSWORD);
    }

    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired LocalIdentitySeeder seeder;

    @Test
    void optionalLocalPasswordIsHashedAndExistingUsersAreNotChanged() {
        var user = users.findByEmail("professor.local@organizacao-recursos.local").orElseThrow();
        String originalHash = user.getPasswordHash();
        assertThat(encoder.matches(PASSWORD, originalHash)).isTrue();
        seeder.run();
        assertThat(users.findByEmail(user.getEmail()).orElseThrow().getPasswordHash()).isEqualTo(originalHash);
        assertThat(users.count()).isEqualTo(3);
    }
}
