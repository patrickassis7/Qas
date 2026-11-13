package br.com.organizacaorecursos.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchemaIsolationWriterIntegrationTest extends PostgresIntegrationTestSupport {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry, SchemaIsolationWriterIntegrationTest.class);
        PostgresTestDatabaseConfiguration.prepareSchemaFor(SchemaIsolationReaderIntegrationTest.class);
    }

    @AfterAll
    static void cleanupCounterpartSchema() {
        PostgresTestDatabaseConfiguration.clean(SchemaIsolationReaderIntegrationTest.class);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void ownsItsMigratedSchemaAndSentinel() {
        String ownSchema = PostgresTestDatabaseConfiguration.schemaNameFor(SchemaIsolationWriterIntegrationTest.class);
        String readerSchema = PostgresTestDatabaseConfiguration.schemaNameFor(SchemaIsolationReaderIntegrationTest.class);

        assertThat(jdbc.queryForObject("SELECT current_schema()", String.class)).isEqualTo(ownSchema);
        assertThat(ownSchema).isNotEqualTo(readerSchema);
        assertThat(jdbc.queryForObject(
                "SELECT max(version) FROM flyway_schema_history WHERE type = 'SQL'", String.class))
                .isEqualTo("6");
        assertThat(jdbc.queryForObject(
                "SELECT owner_name FROM \"" + ownSchema + "\".\"__aiox_test_schema_owner\"", String.class))
                .isEqualTo(SchemaIsolationWriterIntegrationTest.class.getName());
        assertThat(jdbc.queryForObject(
                "SELECT owner_name FROM \"" + readerSchema + "\".\"__aiox_test_schema_owner\"", String.class))
                .isEqualTo(SchemaIsolationReaderIntegrationTest.class.getName());
        assertThat(jdbc.queryForObject(
                "SELECT owner_name FROM \"__aiox_test_schema_owner\"", String.class))
                .isEqualTo(SchemaIsolationWriterIntegrationTest.class.getName());
    }
}
