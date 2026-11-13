package br.com.organizacaorecursos.integration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Shared lifecycle for integration classes backed by a caller-provided local PostgreSQL.
 * Each concrete class must register properties with its own {@code @DynamicPropertySource}
 * method so Spring prepares the schema before constructing the datasource.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PostgresIntegrationTestSupport {

    @RegisterExtension
    protected static final AfterAllCallback SCHEMA_CLEANUP =
            context -> PostgresTestDatabaseConfiguration.clean(context.getRequiredTestClass());

    protected static void registerPostgresProperties(
            DynamicPropertyRegistry registry,
            Class<?> integrationTestClass) {
        PostgresTestDatabaseConfiguration.register(registry, integrationTestClass);
    }
}
