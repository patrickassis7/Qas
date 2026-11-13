package br.com.organizacaorecursos.integration;

import br.com.organizacaorecursos.auth.application.LocalIdentitySeeder;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class LocalSeedIntegrationTest extends PostgresIntegrationTestSupport {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry, LocalSeedIntegrationTest.class);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocalIdentitySeeder localIdentitySeeder;

    @Test
    @Transactional
    void localProfileSeedsFictitiousUsersIdempotently() {
        assertThat(userRepository.count()).isEqualTo(3);

        localIdentitySeeder.run();

        assertThat(userRepository.count()).isEqualTo(3);
    }
}
