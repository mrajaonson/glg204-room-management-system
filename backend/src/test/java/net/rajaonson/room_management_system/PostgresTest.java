package net.rajaonson.room_management_system;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for the tests that need a real database. Requires a running Docker daemon.
 *
 * <p>One single Postgres is started for the whole test run and never stopped, so that every test
 * class reuses it (Testcontainers removes it when the JVM exits). It must not be managed by the
 * {@code @Testcontainers} extension, which would stop it after the first test class while the
 * Spring context cache still points at it.
 *
 * <p>The schema comes from the Flyway migrations of docker/flyway/migrations, as in production;
 * see src/test/resources/application.properties. {@code @DataJpaTest} does not enable Flyway on
 * its own, hence the explicit import of its auto-configuration.
 */
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public abstract class PostgresTest {

    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18-alpine3.24");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
