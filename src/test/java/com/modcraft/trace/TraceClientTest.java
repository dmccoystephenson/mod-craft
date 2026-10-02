package com.modcraft.trace;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the vendored trace client's opt-out order, request shape and server-wide config parsing.
 * The environment is read through the client's {@code environment} seam, never the real one, and
 * every request goes to a loopback stub; nothing here ever reaches the real service.
 */
class TraceClientTest {

    private HttpServer stub;
    private final List<String> bodies = new CopyOnWriteArrayList<>();
    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private final List<String> userAgents = new CopyOnWriteArrayList<>();
    private final Map<String, String> env = new HashMap<>();
    private String endpoint;

    @BeforeEach
    void startStub() throws Exception {
        TraceClient.environment = env::get;
        stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        stub.createContext("/api/metrics", exchange -> {
            try (InputStream in = exchange.getRequestBody()) {
                bodies.add(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            userAgents.add(exchange.getRequestHeaders().getFirst("User-Agent"));
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        stub.start();
        endpoint = "http://127.0.0.1:" + stub.getAddress().getPort();
    }

    @AfterEach
    void stopStub() {
        stub.stop(0);
        TraceClient.environment = System::getenv;
    }

    private TraceClient.Builder builder() {
        return TraceClient.builder(endpoint, "mod-craft", "1.2.3").key("a-key");
    }

    @Test
    void build_enabledWithKeyAndNoOptOut() {
        TraceClient client = builder().build();

        assertTrue(client.isEnabled());
        assertNull(client.disabledReason());
        client.close();
    }

    @Test
    void build_traceUsageReportingOffValuesDisable() {
        for (String value : Arrays.asList("off", "false", "0", "no", " OFF ")) {
            env.put(TraceClient.ENV_USAGE_REPORTING, value);
            TraceClient client = builder().build();

            assertFalse(client.isEnabled(), value);
            assertEquals(TraceClient.REASON_ENVIRONMENT, client.disabledReason(), value);
        }
    }

    @Test
    void build_doNotTrackYesValuesDisable() {
        for (String value : Arrays.asList("1", "true", "yes", "TRUE")) {
            env.put(TraceClient.ENV_DO_NOT_TRACK, value);
            TraceClient client = builder().build();

            assertFalse(client.isEnabled(), value);
            assertEquals(TraceClient.REASON_ENVIRONMENT, client.disabledReason(), value);
        }
    }

    @Test
    void build_unrecognisedEnvironmentValuesLeaveReportingOn() {
        env.put(TraceClient.ENV_USAGE_REPORTING, "on");
        env.put(TraceClient.ENV_DO_NOT_TRACK, "0");
        TraceClient client = builder().build();

        assertTrue(client.isEnabled());
        client.close();
    }

    @Test
    void build_environmentWinsOverDisabledSettingAndMissingKey() {
        env.put(TraceClient.ENV_DO_NOT_TRACK, "1");
        TraceClient client = TraceClient.builder(endpoint, "mod-craft", "1.2.3").enabled(false).build();

        assertEquals(TraceClient.REASON_ENVIRONMENT, client.disabledReason());
    }

    @Test
    void build_disabledSettingWinsOverMissingKey() {
        TraceClient client = TraceClient.builder(endpoint, "mod-craft", "1.2.3").enabled(false).build();

        assertEquals(TraceClient.REASON_CONFIG, client.disabledReason());
    }

    @Test
    void build_blankKeyDisables() {
        assertEquals(TraceClient.REASON_NO_KEY, builder().key(null).build().disabledReason());
        assertEquals(TraceClient.REASON_NO_KEY, builder().key("   ").build().disabledReason());
    }

    @Test
    void disabled_reportsNothingAndCloseIsSafe() {
        TraceClient client = TraceClient.disabled();
        client.report("startup");
        client.close();
        client.close();

        assertFalse(client.isEnabled());
        assertEquals(TraceClient.REASON_CONFIG, client.disabledReason());
    }

    @Test
    void builder_rejectsMissingArguments() {
        assertThrows(IllegalArgumentException.class, () -> TraceClient.builder(" ", "mod-craft", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> TraceClient.builder(endpoint, null, "1.0"));
        assertThrows(IllegalArgumentException.class, () -> TraceClient.builder(endpoint, "mod-craft", ""));
    }

    @Test
    void builder_rejectsVersionLongerThanTagLimit() {
        String tooLong = "v".repeat(TraceClient.MAX_TAG_LENGTH + 1);

        assertThrows(IllegalArgumentException.class, () -> TraceClient.builder(endpoint, "mod-craft", tooLong));
    }

    @Test
    void report_postsEventWithKeyUserAgentAndVersionTag() {
        TraceClient client = TraceClient.builder(endpoint + "//", " mod-craft ", " 1.2.3 ").key("a-key").build();
        client.report("command", 2.5, Map.of("name", "list"));
        client.close();

        assertEquals(1, bodies.size());
        assertEquals("{\"application\":\"mod-craft\",\"name\":\"command\",\"value\":2.5,"
                + "\"tags\":{\"name\":\"list\",\"version\":\"1.2.3\"}}", bodies.get(0));
        assertEquals("Bearer a-key", authorizations.get(0));
        assertEquals("trace-client/" + TraceClient.VERSION + " (mod-craft)", userAgents.get(0));
    }

    @Test
    void report_blankNameSendsNothing() {
        TraceClient client = builder().build();
        client.report(null);
        client.report("  ");
        client.close();

        assertTrue(bodies.isEmpty());
    }

    @Test
    void report_unreachableServerNeverThrows() {
        stub.stop(0);
        TraceClient client = builder().build();
        client.report("startup");
        client.close();

        assertTrue(bodies.isEmpty());
    }

    @Test
    void json_omitsNonFiniteValueAndNullTags() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("kept", "yes");
        tags.put("dropped", null);

        assertEquals("{\"application\":\"a\",\"name\":\"n\",\"tags\":{\"kept\":\"yes\"}}",
                TraceClient.json("a", "n", Double.NaN, tags));
        assertEquals("{\"application\":\"a\",\"name\":\"n\"}",
                TraceClient.json("a", "n", Double.POSITIVE_INFINITY, null));
    }

    @Test
    void quote_escapesQuotesBackslashesAndControlCharacters() {
        assertEquals("\"a\\\"b\\\\c\\nd\\re\\tf\\u0001\"", TraceClient.quote("a\"b\\c\nd\re\tf\u0001"));
    }

    @Test
    void withVersion_addsVersionWithoutOverridingOrMutating() {
        Map<String, String> own = new LinkedHashMap<>();
        own.put("version", "custom");

        assertEquals(Map.of("version", "custom"), TraceClient.withVersion(own, "1.0"));
        assertEquals(Map.of("version", "1.0"), TraceClient.withVersion(null, "1.0"));
        assertEquals(Map.of("version", "custom"), own);
    }

    @Test
    void withServerWideTags_eventTagWinsAndMergeStopsAtMaxTags() {
        Map<String, String> own = new LinkedHashMap<>();
        own.put("ci", "false");
        Map<String, String> serverWide = new LinkedHashMap<>();
        serverWide.put("ci", "true");
        serverWide.put("site", "test");

        assertEquals(Map.of("ci", "false", "site", "test"), TraceClient.withServerWideTags(own, serverWide));

        Map<String, String> full = new LinkedHashMap<>();
        for (int i = 0; i < TraceClient.MAX_TAGS; i++) {
            full.put("k" + i, "v");
        }
        assertEquals(TraceClient.MAX_TAGS, TraceClient.withServerWideTags(full, serverWide).size());
        assertFalse(TraceClient.withServerWideTags(full, serverWide).containsKey("site"));
    }

    @Test
    void parseServerWideConfig_readsSwitchAndTagsBlock() {
        TraceClient.ServerWideConfig config = TraceClient.parseServerWideConfig(Arrays.asList(
                "# comment",
                "enabled: false",
                "tags:",
                "  ci: \"true\"",
                "  site: 'it''s'   # trailing comment",
                "  bare: value # comment",
                "    deeper: ignored",
                "  bad key: dropped",
                "  nospace:dropped",
                "  empty:",
                "  flow: [a, b]",
                "enabled: true"));

        assertTrue(config.disables);
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("ci", "true");
        expected.put("site", "it's");
        expected.put("bare", "value");
        assertEquals(expected, config.tags);
    }

    @Test
    void parseServerWideConfig_enabledInsideTagsBlockIsATagNotTheSwitch() {
        TraceClient.ServerWideConfig config = TraceClient.parseServerWideConfig(Arrays.asList(
                "tags:",
                "  enabled: false"));

        assertFalse(config.disables);
        assertEquals(Map.of("enabled", "false"), config.tags);
    }

    @Test
    void serverWideConfig_missingFileIsCreatedAndLeavesReportingOn(@TempDir Path plugins) throws Exception {
        TraceClient client = builder().serverWideConfig(plugins.toFile()).build();

        Path file = plugins.resolve("trace").resolve("config.yml");
        assertEquals(TraceClient.SERVER_WIDE_CONFIG_CONTENT, Files.readString(file));
        assertTrue(client.isEnabled());
        client.close();
    }

    @Test
    void serverWideConfig_switchOffWinsOverEnabledSetting(@TempDir Path plugins) throws Exception {
        Path file = plugins.resolve("trace").resolve("config.yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "enabled: false\n");

        TraceClient client = builder().serverWideConfig(plugins.toFile()).build();

        assertEquals(TraceClient.REASON_SERVER_WIDE, client.disabledReason());
        assertEquals("enabled: false\n", Files.readString(file), "an existing file is never rewritten");
    }

    @Test
    void serverWideConfig_tagsAreAddedToEveryEvent(@TempDir Path plugins) throws Exception {
        Path file = plugins.resolve("trace").resolve("config.yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "enabled: true\ntags:\n  ci: \"true\"\n");

        TraceClient client = builder().serverWideConfig(plugins.toFile()).build();
        client.report("startup");
        client.close();

        assertEquals("{\"application\":\"mod-craft\",\"name\":\"startup\","
                + "\"tags\":{\"version\":\"1.2.3\",\"ci\":\"true\"}}", bodies.get(0));
    }

    @Test
    void serverWideConfig_unreadableDirectoryCountsAsEnabled(@TempDir Path plugins) throws Exception {
        File notADirectory = plugins.resolve("plugins-file").toFile();
        Files.writeString(notADirectory.toPath(), "not a directory");

        TraceClient client = builder().serverWideConfig(notADirectory).build();

        assertTrue(client.isEnabled());
        client.close();
    }
}
