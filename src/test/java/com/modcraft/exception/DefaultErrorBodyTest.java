package com.modcraft.exception;

import com.modcraft.model.Mod;
import com.modcraft.model.Modpack;
import com.modcraft.repository.ModRepository;
import com.modcraft.repository.ModpackRepository;
import com.modcraft.service.ModpackBuilderService;
import com.modcraft.service.ModService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The error bodies {@code COMMANDS.md} attributes to Spring Boot rather than to the API. They are
 * written by the servlet container's error dispatch, which MockMvc does not perform, so these
 * tests send real requests to the application on a random loopback port.
 *
 * <p>Nothing here runs inside a test-wide transaction, so {@link #deleteCommittedRows()} removes
 * whatever a test commits; the other test classes share this database and count rows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultErrorBodyTest {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
        new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ModService modService;

    @Autowired
    private ModpackBuilderService modpackBuilderService;

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private ModpackRepository modpackRepository;

    @AfterEach
    void deleteCommittedRows() {
        modpackRepository.deleteAll();
        modRepository.deleteAll();
    }

    @Test
    void malformedJsonBody_returns400WithDefaultBody() {
        ResponseEntity<Map<String, Object>> response =
            exchange(HttpMethod.POST, "/api/mods", MediaType.APPLICATION_JSON, "{not json");

        assertDefaultBody(response, HttpStatus.BAD_REQUEST, "/api/mods");
    }

    @Test
    void nonNumericPathId_returns400WithDefaultBody() {
        ResponseEntity<Map<String, Object>> response = exchange(HttpMethod.GET, "/api/mods/abc", null, null);

        assertDefaultBody(response, HttpStatus.BAD_REQUEST, "/api/mods/abc");
    }

    @Test
    void unsupportedContentType_returns415WithDefaultBody() {
        ResponseEntity<Map<String, Object>> response =
            exchange(HttpMethod.POST, "/api/mods", MediaType.TEXT_PLAIN, "name=Sodium");

        assertDefaultBody(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "/api/mods");
    }

    @Test
    void unsupportedMethod_returns405WithDefaultBody() {
        ResponseEntity<Map<String, Object>> response =
            exchange(HttpMethod.PUT, "/api/mods", MediaType.APPLICATION_JSON, "{}");

        assertDefaultBody(response, HttpStatus.METHOD_NOT_ALLOWED, "/api/mods");
    }

    @Test
    void unknownPath_returns404WithDefaultBody() {
        ResponseEntity<Map<String, Object>> response = exchange(HttpMethod.GET, "/api/unknown", null, null);

        assertDefaultBody(response, HttpStatus.NOT_FOUND, "/api/unknown");
    }

    /**
     * The real {@code 500} behind the documented failure tracked in issue #14. Expect to replace
     * this test, along with {@code CommittedRequestsTest.deleteMod_stillInModpackFailsAndChangesNothing},
     * with that fix.
     */
    @Test
    void deleteMod_stillInModpack_returns500WithDefaultBody() {
        Mod mod = modService.createMod(
            new Mod("Sodium", "0.5.3", "jellysquid", "Renderer", "http://example.com/sodium.jar", "1.20.1")
        );
        Modpack modpack = modpackBuilderService.createModpack(new Modpack("Error Pack", null, "1.20.1"));
        modpackBuilderService.addModToModpack(modpack.getId(), mod.getId());
        String path = "/api/mods/" + mod.getId();

        ResponseEntity<Map<String, Object>> response = exchange(HttpMethod.DELETE, path, null, null);

        assertDefaultBody(response, HttpStatus.INTERNAL_SERVER_ERROR, path);
    }

    /** The contrast: a {@code 404} the API raises itself keeps its single {@code error} field. */
    @Test
    void missingMod_returns404WithSingleErrorField() {
        ResponseEntity<Map<String, Object>> response = exchange(HttpMethod.GET, "/api/mods/987654", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsOnly(Map.entry("error", "Mod not found with id: 987654"));
    }

    private ResponseEntity<Map<String, Object>> exchange(
            HttpMethod method, String path, MediaType contentType, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (contentType != null) {
            headers.setContentType(contentType);
        }
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), JSON_OBJECT);
    }

    private static void assertDefaultBody(
            ResponseEntity<Map<String, Object>> response, HttpStatus status, String path) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody())
            .containsOnlyKeys("timestamp", "status", "error", "path")
            .containsEntry("status", status.value())
            .containsEntry("error", status.getReasonPhrase())
            .containsEntry("path", path);
    }
}
