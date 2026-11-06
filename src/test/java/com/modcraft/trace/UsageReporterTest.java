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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Exercises the reporter against a loopback stub; nothing here ever reaches the real service.
 *
 * <p>An enabled reporter keeps its installation ID in the per-user data directory, which
 * {@link TraceInstallId} derives from {@code user.home}, so each test points that property at a
 * temporary directory. {@code XDG_DATA_HOME} and {@code APPDATA} win over it and cannot be changed
 * from a running JVM; {@link #assumeInstallIdStaysOutOfTheRealDataDirectory()} skips the tests that
 * would otherwise write there.
 */
class UsageReporterTest {

    private HttpServer stub;
    private final List<String> bodies = new CopyOnWriteArrayList<>();
    private String endpoint;
    private String realUserHome;

    @TempDir
    Path temporaryHome;

    @BeforeEach
    void redirectUserDataDirectory() {
        realUserHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryHome.toString());
    }

    @AfterEach
    void restoreUserHome() {
        System.setProperty("user.home", realUserHome);
    }

    @BeforeEach
    void startStub() throws Exception {
        stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        stub.createContext("/api/metrics", exchange -> {
            try (InputStream in = exchange.getRequestBody()) {
                bodies.add(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        stub.start();
        endpoint = "http://127.0.0.1:" + stub.getAddress().getPort();
    }

    @AfterEach
    void stopStub() {
        stub.stop(0);
    }

    private static boolean environmentOptsOut() {
        String trace = System.getenv(TraceClient.ENV_USAGE_REPORTING);
        String dnt = System.getenv(TraceClient.ENV_DO_NOT_TRACK);
        return (trace != null && !trace.isBlank()) || (dnt != null && !dnt.isBlank());
    }

    private File installIdFile() {
        return TraceInstallId.file(UsageReporter.APPLICATION);
    }

    /** False where {@code XDG_DATA_HOME} or {@code APPDATA} picks the data directory instead. */
    private static boolean userHomeDecidesTheDataDirectory() {
        Path home = Path.of("any-home");
        return TraceInstallId.file(UsageReporter.APPLICATION, System::getenv, System.getProperty("os.name", ""), home.toString())
                .toPath().startsWith(home);
    }

    /** {@code TRACE_INSTALL_ID} pins the ID, in which case the client never touches the file. */
    private void assumeInstallIdStaysOutOfTheRealDataDirectory() {
        assumeTrue(TraceInstallId.fromEnvironment() != null || installIdFile().toPath().startsWith(temporaryHome),
                "XDG_DATA_HOME or APPDATA overrides user.home, so an enabled reporter would write to " + installIdFile());
    }

    @Test
    void keepsTheInstallationIdFileInTheRedirectedDataDirectory() throws Exception {
        assumeFalse(environmentOptsOut(), "the environment has switched reporting off");
        assumeTrue(TraceInstallId.fromEnvironment() == null, "TRACE_INSTALL_ID pins the ID, so there is no file");
        assumeTrue(userHomeDecidesTheDataDirectory(), "XDG_DATA_HOME or APPDATA overrides user.home");
        assertTrue(installIdFile().toPath().startsWith(temporaryHome),
                "expected " + installIdFile() + " under " + temporaryHome);

        UsageReporter reporter = new UsageReporter(true, endpoint, "a-key", "1.0");
        reporter.close();

        assertEquals(reporter.installId(), Files.readString(installIdFile().toPath()).trim());
    }

    @Test
    void carriesARandomInstallationIdOnlyWhileReportingIsOn() {
        assumeInstallIdStaysOutOfTheRealDataDirectory();
        UsageReporter on = new UsageReporter(true, endpoint, "a-key", "1.0");
        UsageReporter off = new UsageReporter(false, endpoint, "a-key", "1.0");

        assertNotNull(on.installId(), "an enabled client carries a random installation ID");
        assertNull(off.installId(), "a disabled client never makes up an ID");
        on.close();
        off.close();
    }

    @Test
    void disabledSettingSendsNothing() {
        UsageReporter reporter = new UsageReporter(false, endpoint, "a-key", "1.0");
        reporter.reportStartup();
        reporter.close();

        assertFalse(reporter.isReporting());
        assertTrue(bodies.isEmpty());
    }

    @Test
    void blankKeySendsNothing() {
        UsageReporter reporter = new UsageReporter(true, endpoint, "", "1.0");
        reporter.reportStartup();
        reporter.close();

        assertFalse(reporter.isReporting());
        assertTrue(bodies.isEmpty());
    }

    @Test
    void blankEndpointSendsNothing() {
        UsageReporter reporter = new UsageReporter(true, "  ", "a-key", "1.0");
        reporter.reportStartup();
        reporter.close();

        assertFalse(reporter.isReporting());
        assertTrue(bodies.isEmpty());
    }

    /**
     * Pins the behaviour tracked in issue #30: only a blank endpoint is rejected, so a malformed
     * one leaves the reporter on and the event is dropped when it is sent. Expect to change this
     * test if that issue is resolved by validating the endpoint.
     */
    @Test
    void malformedEndpointStaysOnAndDropsTheEvent() {
        assumeFalse(environmentOptsOut(), "the environment has switched reporting off");
        assumeInstallIdStaysOutOfTheRealDataDirectory();
        UsageReporter reporter = new UsageReporter(true, "not a url", "a-key", "1.0");
        reporter.reportStartup();
        reporter.close();

        assertTrue(reporter.isReporting());
        assertTrue(bodies.isEmpty());
    }

    @Test
    void unfilteredVersionPlaceholderIsNotSent() {
        UsageReporter reporter = new UsageReporter(false, endpoint, "a-key", "@project.version@");
        // Outside a packaged jar there is no manifest version either.
        assertEquals("unknown", reporter.version());
        assertEquals("true", reporter.startupTags().get("service"));
        assertFalse(reporter.startupTags().containsKey("version"), "the client adds version itself");
    }

    @Test
    void sendsOneStartupEventWithServiceAndVersionTags() {
        if (environmentOptsOut()) {
            return; // the environment has switched reporting off; that path is the client's to test
        }
        assumeInstallIdStaysOutOfTheRealDataDirectory();
        UsageReporter reporter = new UsageReporter(true, endpoint, "a-key", "1.2.3");
        reporter.reportStartup();
        reporter.close();

        assertEquals(1, bodies.size());
        assertEquals("{\"application\":\"mod-craft\",\"name\":\"startup\","
                + "\"tags\":{\"service\":\"true\",\"version\":\"1.2.3\",\"install\":\"" + reporter.installId() + "\"}}", bodies.get(0));
    }
}
