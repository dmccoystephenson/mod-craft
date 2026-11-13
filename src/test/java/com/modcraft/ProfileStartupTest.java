package com.modcraft;

import com.modcraft.trace.UsageReporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Start-up of the application with the production configuration in
 * {@code src/main/resources}, as the README's "Run the application" step does it.
 *
 * <p>The test {@code application.properties} shadows the main one on the classpath, so every
 * other test runs without it. These tests point {@code spring.config.location} at
 * {@code src/main/resources} instead, so the real defaults and the real {@code dev} profile are
 * loaded. {@code usage-reporting.enabled=false} is passed as a command-line argument, which wins
 * over the main file's {@code ${USAGE_REPORTING_ENABLED:true}}, so no start-up here contacts the
 * trace service. Each context is closed before the test returns. With {@code create-drop}, that
 * drops the schema from the shared in-memory {@code modcraft} database again.
 */
class ProfileStartupTest {

    private static final String MAIN_CONFIG = "--spring.config.location=file:src/main/resources/";
    private static final String NO_USAGE_REPORTING = "--usage-reporting.enabled=false";
    private static final String RANDOM_PORT = "--server.port=0";

    @Test
    void devProfile_startsAgainstTheDefaultInMemoryDatabase() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ModCraftApplication.class)
                .profiles("dev")
                .run(MAIN_CONFIG, NO_USAGE_REPORTING, RANDOM_PORT)) {
            Environment env = context.getEnvironment();

            // The main file was loaded, not the test one: this URL is only set there.
            assertThat(env.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:h2:mem:modcraft;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
            assertThat(env.getProperty("spring.h2.console.enabled")).isEqualTo("true");
            assertThat(context.getBean(UsageReporter.class).isReporting()).isFalse();

            // USER_GUIDE.md "Verify the service is running": an empty JSON array.
            HttpResponse<String> mods = get(env, "/api/mods");
            assertThat(mods.statusCode()).isEqualTo(200);
            assertThat(mods.body()).isEqualTo("[]");

            // CONFIG.md: the dev profile serves the H2 console at spring.h2.console.path.
            assertThat(get(env, "/h2-console/").statusCode()).isEqualTo(200);
        }
    }

    /**
     * Characterizes the current behaviour reported in issue #7: the default profile validates
     * the schema against an in-memory database that nothing creates, so start-up fails. When #7
     * is fixed, this test is expected to change to assert a successful start-up.
     */
    @Test
    void defaultProfile_failsSchemaValidationAgainstTheEmptyInMemoryDatabase() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(ModCraftApplication.class)
                .run(MAIN_CONFIG, NO_USAGE_REPORTING, RANDOM_PORT)
                .close())
            .rootCause()
            .hasMessageStartingWith("Schema-validation: missing table");
    }

    private static HttpResponse<String> get(Environment env, String path) throws Exception {
        URI uri = URI.create("http://localhost:" + env.getProperty("local.server.port") + path);
        HttpClient client = HttpClient.newHttpClient();
        return client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
