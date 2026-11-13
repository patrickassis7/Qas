package br.com.organizacaorecursos.integration;

import org.postgresql.Driver;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates and prepares an isolated local database schema before Spring constructs its
 * datasource. The test process intentionally has no fallback to application or Supabase URLs.
 */
final class PostgresTestDatabaseConfiguration {

    private static final String SCHEMA_MARKER_TABLE = "__aiox_test_schema_owner";
    private static final List<String> FORBIDDEN_ENVIRONMENT = List.of(
            "SUPABASE_DB_URL",
            "SPRING_DATASOURCE_URL",
            "SPRING_DATASOURCE_USERNAME",
            "SPRING_DATASOURCE_PASSWORD",
            "SPRING_DATASOURCE_JNDI_NAME",
            "SPRING_DATASOURCE_HIKARI_JDBC_URL",
            "SPRING_FLYWAY_URL",
            "SPRING_FLYWAY_USER",
            "SPRING_FLYWAY_PASSWORD",
            "SPRING_APPLICATION_JSON",
            "SPRING_CONFIG_LOCATION",
            "SPRING_CONFIG_ADDITIONAL_LOCATION",
            "SPRING_CONFIG_IMPORT",
            "JDBC_DATABASE_URL",
            "DATABASE_URL",
            "DB_URL",
            "DB_USERNAME",
            "DB_PASSWORD",
            "PGHOST",
            "PGPORT",
            "PGDATABASE",
            "PGUSER",
            "PGPASSWORD",
            "PGHOSTADDR",
            "PGOPTIONS",
            "PGSERVICE",
            "PGSERVICEFILE");
    private static final List<String> FORBIDDEN_SYSTEM_PROPERTIES = List.of(
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.datasource.jndi-name",
            "spring.datasource.hikari.jdbc-url",
            "spring.flyway.url",
            "spring.flyway.user",
            "spring.flyway.password",
            "spring.application.json",
            "spring.config.location",
            "spring.config.additional-location",
            "spring.config.import");

    private static final Object LOCK = new Object();
    private static final Map<Class<?>, PreparedSchema> PREPARED_BY_CLASS = new ConcurrentHashMap<>();
    private static final Map<String, String> CLASS_BY_SCHEMA = new ConcurrentHashMap<>();

    private PostgresTestDatabaseConfiguration() {
    }

    static void register(DynamicPropertyRegistry registry, Class<?> integrationTestClass) {
        PreparedSchema prepared = prepare(integrationTestClass, System.getenv(), System.getProperties());
        registry.add("spring.datasource.url", prepared::jdbcUrl);
        registry.add("spring.datasource.username", prepared.settings()::username);
        registry.add("spring.datasource.password", prepared.settings()::password);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("spring.flyway.baseline-version", () -> "0");
        registry.add("spring.flyway.schemas", prepared::schemaName);
        registry.add("spring.flyway.default-schema", prepared::schemaName);
        registry.add("spring.jpa.properties.hibernate.default_schema", prepared::schemaName);
    }

    static String validateJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
            throw invalidConfiguration();
        }

        try {
            URI uri = new URI(jdbcUrl.substring("jdbc:".length()));
            String host = uri.getHost();
            if (!"postgresql".equalsIgnoreCase(uri.getScheme())
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || host == null
                    || uri.getPort() < 1
                    || uri.getPort() > 65_535
                    || uri.getRawPath() == null
                    || uri.getRawPath().length() < 2
                    || !isLoopbackAddressLiteral(host)) {
                throw invalidConfiguration();
            }
            if (new Driver().parseURL(jdbcUrl, new Properties()) == null) {
                throw invalidConfiguration();
            }
            return jdbcUrl;
        } catch (URISyntaxException | IllegalArgumentException exception) {
            throw invalidConfiguration();
        }
    }

    static String schemaNameFor(Class<?> integrationTestClass) {
        String simpleName = integrationTestClass.getSimpleName()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
        if (simpleName.isBlank() || !Character.isLetter(simpleName.charAt(0))) {
            simpleName = "test_" + simpleName;
        }
        simpleName = simpleName.substring(0, Math.min(simpleName.length(), 34));
        return "it_" + simpleName + "_" + sha256Prefix(integrationTestClass.getName());
    }

    static void prepareSchemaFor(Class<?> integrationTestClass) {
        prepare(integrationTestClass, System.getenv(), System.getProperties());
    }

    static void clean(Class<?> integrationTestClass) {
        synchronized (LOCK) {
            PreparedSchema prepared = PREPARED_BY_CLASS.get(integrationTestClass);
            if (prepared == null) {
                return;
            }
            try (Connection connection = DriverManager.getConnection(
                    prepared.settings().url(),
                    prepared.settings().username(),
                    prepared.settings().password())) {
                if (schemaExists(connection, prepared.schemaName())) {
                    verifySchemaOwner(connection, prepared);
                    execute(connection, "DROP SCHEMA " + quoteIdentifier(prepared.schemaName()) + " CASCADE");
                }
            } catch (SQLException exception) {
                throw new IllegalStateException(
                        "Could not clean the isolated local PostgreSQL test schema.");
            } finally {
                PREPARED_BY_CLASS.remove(integrationTestClass, prepared);
                CLASS_BY_SCHEMA.remove(prepared.schemaName(), integrationTestClass.getName());
            }
        }
    }

    static Settings settingsFrom(Map<String, String> environment, Properties systemProperties) {
        for (String name : FORBIDDEN_ENVIRONMENT) {
            if (environment.containsKey(name)) {
                throw new IllegalStateException(
                        "Integration tests reject external datasource configuration; remove " + name + ".");
            }
        }
        for (String name : FORBIDDEN_SYSTEM_PROPERTIES) {
            if (systemProperties.containsKey(name)) {
                throw new IllegalStateException(
                        "Integration tests reject external datasource system properties.");
            }
        }
        for (String name : FORBIDDEN_ENVIRONMENT) {
            if (systemProperties.containsKey(name)) {
                throw new IllegalStateException(
                        "Integration tests reject external datasource system properties.");
            }
        }

        String url = environment.get("TEST_DB_URL");
        String username = environment.get("TEST_DB_USERNAME");
        String password = environment.get("TEST_DB_PASSWORD");
        if (url == null || url.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Integration tests require TEST_DB_URL, TEST_DB_USERNAME, and TEST_DB_PASSWORD.");
        }
        return new Settings(validateJdbcUrl(url), username, password);
    }

    private static PreparedSchema prepare(
            Class<?> integrationTestClass,
            Map<String, String> environment,
            Properties systemProperties) {
        synchronized (LOCK) {
            PreparedSchema existing = PREPARED_BY_CLASS.get(integrationTestClass);
            if (existing != null) {
                return existing;
            }

            Settings settings = settingsFrom(environment, systemProperties);
            String schemaName = schemaNameFor(integrationTestClass);
            String owner = CLASS_BY_SCHEMA.putIfAbsent(schemaName, integrationTestClass.getName());
            if (owner != null && !owner.equals(integrationTestClass.getName())) {
                throw new IllegalStateException(
                        "Integration test schema-name collision detected; no database connection was opened.");
            }

            PreparedSchema prepared = new PreparedSchema(settings, schemaName, integrationTestClass.getName());
            prepareSchema(prepared);
            PREPARED_BY_CLASS.put(integrationTestClass, prepared);
            return prepared;
        }
    }

    private static void prepareSchema(PreparedSchema prepared) {
        try (Connection connection = DriverManager.getConnection(
                prepared.settings().url(),
                prepared.settings().username(),
                prepared.settings().password())) {
            connection.setAutoCommit(false);
            try {
                boolean exists = schemaExists(connection, prepared.schemaName());
                if (exists) {
                    verifySchemaOwner(connection, prepared);
                    execute(connection, "DROP SCHEMA " + quoteIdentifier(prepared.schemaName()) + " CASCADE");
                }

                execute(connection, "CREATE SCHEMA " + quoteIdentifier(prepared.schemaName()));
                execute(connection, "CREATE TABLE " + quoteIdentifier(prepared.schemaName()) + "."
                        + quoteIdentifier(SCHEMA_MARKER_TABLE) + " (owner_name text NOT NULL)");
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO " + quoteIdentifier(prepared.schemaName()) + "."
                                + quoteIdentifier(SCHEMA_MARKER_TABLE) + " (owner_name) VALUES (?)")) {
                    insert.setString(1, prepared.ownerName());
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            CLASS_BY_SCHEMA.remove(prepared.schemaName(), prepared.ownerName());
            throw new IllegalStateException(
                    "Could not prepare the isolated local PostgreSQL test schema; check database availability and permissions.");
        }
    }

    private static boolean schemaExists(Connection connection, String schemaName) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = ?)")) {
            query.setString(1, schemaName);
            try (ResultSet result = query.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private static void verifySchemaOwner(Connection connection, PreparedSchema prepared) throws SQLException {
        String markerQuery = "SELECT owner_name FROM " + quoteIdentifier(prepared.schemaName()) + "."
                + quoteIdentifier(SCHEMA_MARKER_TABLE);
        try (Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(markerQuery)) {
            if (!result.next() || !prepared.ownerName().equals(result.getString(1))) {
                throw new IllegalStateException("Existing test schema ownership did not match; schema was not changed.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Existing test schema has no valid ownership marker; schema was not changed.");
        }
    }

    private static boolean isLoopbackAddressLiteral(String host) {
        if ("127.0.0.1".equals(host)) {
            return true;
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            String ipv6Literal = host.substring(1, host.length() - 1);
            if (!ipv6Literal.contains("%")) {
                try {
                    InetAddress address = InetAddress.getByName(ipv6Literal);
                    return address instanceof Inet6Address && address.isLoopbackAddress();
                } catch (Exception ignored) {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    private static String quoteIdentifier(String identifier) {
        if (!identifier.matches("[a-z_][a-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid generated test identifier.");
        }
        return "\"" + identifier + "\"";
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String sha256Prefix(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable; cannot name an isolated schema.");
        }
    }

    private static IllegalArgumentException invalidConfiguration() {
        return new IllegalArgumentException(
                "TEST_DB_URL must be a valid PostgreSQL JDBC URL using an IP literal for loopback and no query options.");
    }

    record Settings(String url, String username, String password) {
        @Override
        public String toString() {
            return "Settings[url=<redacted>, username=<redacted>, password=<redacted>]";
        }
    }

    private record PreparedSchema(Settings settings, String schemaName, String ownerName) {
        private String jdbcUrl() {
            return settings.url() + "?currentSchema=" + schemaName;
        }
    }
}
