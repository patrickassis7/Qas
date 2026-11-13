package br.com.organizacaorecursos.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresTestDatabaseConfigurationTest {

    @Test
    void acceptsOnlyPostgresUrlsWithLiteralLoopbackHosts() {
        assertThat(PostgresTestDatabaseConfiguration.validateJdbcUrl(
                "jdbc:postgresql://127.0.0.1:5432/test_db"))
                .isEqualTo("jdbc:postgresql://127.0.0.1:5432/test_db");
        assertThat(PostgresTestDatabaseConfiguration.validateJdbcUrl(
                "jdbc:postgresql://[::1]:5432/test_db"))
                .isEqualTo("jdbc:postgresql://[::1]:5432/test_db");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-jdbc-url",
            "jdbc:mysql://127.0.0.1:5432/test_db",
            "jdbc:postgresql://localhost:5432/test_db",
            "jdbc:postgresql://db.example.test:5432/test_db",
            "jdbc:postgresql://203.0.113.8:5432/test_db",
            "jdbc:postgresql://127.8.4.2:5432/test_db",
            "jdbc:postgresql://supabase.example.test:5432/test_db",
            "jdbc:postgresql://127.0.0.1/test_db",
            "jdbc:postgresql://127.0.0.1:5432/",
            "jdbc:postgresql://user:secret@127.0.0.1:5432/test_db",
            "jdbc:postgresql://127.0.0.1:5432/test_db?host=remote.example.test",
            "jdbc:postgresql://127.0.0.1:99999/test_db"
    })
    void rejectsMalformedHostnameRemoteAndOverriddenUrlsWithoutEchoingInput(String url) {
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.validateJdbcUrl(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining(url)
                .hasMessageNotContaining("secret");
    }

    @Test
    void rejectsSupabaseAndEquivalentExternalDatasourceConfigurationBeforeConnecting() {
        String secretLikeValue = "postgresql://sensitive.example.test/db-secret";
        Map<String, String> supabaseEnvironment = new HashMap<>(validEnvironment());
        supabaseEnvironment.put("SUPABASE_DB_URL", secretLikeValue);

        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(
                supabaseEnvironment, new Properties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(secretLikeValue);

        Map<String, String> pgHostEnvironment = new HashMap<>(validEnvironment());
        pgHostEnvironment.put("PGHOST", "external.example.test");
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(pgHostEnvironment, new Properties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining("external.example.test");

        Properties externalSpringConfiguration = new Properties();
        externalSpringConfiguration.setProperty("spring.datasource.url", secretLikeValue);
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(
                validEnvironment(), externalSpringConfiguration))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(secretLikeValue);

        Properties externalSupabaseConfiguration = new Properties();
        externalSupabaseConfiguration.setProperty("SUPABASE_DB_URL", secretLikeValue);
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(
                validEnvironment(), externalSupabaseConfiguration))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(secretLikeValue);
    }

    @Test
    void requiresOnlyExplicitTestDatabaseEnvironmentVariablesAndNoFallback() {
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(Map.of(), new Properties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_DB_URL")
                .hasMessageNotContaining("SUPABASE_DB_URL");

        Map<String, String> incomplete = Map.of(
                "TEST_DB_URL", "jdbc:postgresql://127.0.0.1:5432/test_db",
                "TEST_DB_USERNAME", "test_user");
        assertThatThrownBy(() -> PostgresTestDatabaseConfiguration.settingsFrom(incomplete, new Properties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TEST_DB_PASSWORD");
    }

    @Test
    void derivesStableValidAndDistinctSchemasForDifferentClasses() {
        String first = PostgresTestDatabaseConfiguration.schemaNameFor(SchemaIsolationWriterIntegrationTest.class);
        String repeated = PostgresTestDatabaseConfiguration.schemaNameFor(SchemaIsolationWriterIntegrationTest.class);
        String second = PostgresTestDatabaseConfiguration.schemaNameFor(SchemaIsolationReaderIntegrationTest.class);

        assertThat(first).isEqualTo(repeated).matches("[a-z][a-z0-9_]{1,62}");
        assertThat(second).matches("[a-z][a-z0-9_]{1,62}").isNotEqualTo(first);
    }

    private static Map<String, String> validEnvironment() {
        return Map.of(
                "TEST_DB_URL", "jdbc:postgresql://127.0.0.1:5432/test_db",
                "TEST_DB_USERNAME", "test_user",
                "TEST_DB_PASSWORD", "synthetic-password-for-unit-test");
    }
}
