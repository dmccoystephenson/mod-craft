package com.modcraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcraft.model.Mod;
import com.modcraft.model.Modpack;
import com.modcraft.repository.ModRepository;
import com.modcraft.repository.ModpackRepository;
import com.modcraft.service.ModpackBuilderService;
import com.modcraft.service.ModService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Requests that really commit, as they do in production. Unlike the other controller tests this
 * class is deliberately not {@code @Transactional}: each service call commits on its own, and each
 * read goes to the database outside any service transaction. That brings flush-time constraints
 * and lazy loading into play, both of which a test-wide transaction hides.
 *
 * <p>The rows outlive each test, so {@link #deleteCommittedRows()} removes them; the other test
 * classes share this database and count rows.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CommittedRequestsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModService modService;

    @Autowired
    private ModpackBuilderService modpackBuilderService;

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private ModpackRepository modpackRepository;

    private Mod savedMod;
    private Modpack savedModpack;

    @BeforeEach
    void setUp() {
        savedMod = modService.createMod(
            new Mod("Sodium", "0.5.3", "jellysquid", "Renderer", "http://example.com/sodium.jar", "1.20.1")
        );
        savedModpack = modpackBuilderService.createModpack(
            new Modpack("Committed Pack", "Outlives its transaction", "1.20.1")
        );
    }

    @AfterEach
    void deleteCommittedRows() {
        modpackRepository.deleteAll();
        modRepository.deleteAll();
    }

    @Test
    void addModToModpack_isReturnedByEveryLaterRead() throws Exception {
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}", savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods", hasSize(1)));

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods[0].name", is("Sodium")));
        mockMvc.perform(get("/api/modpacks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].mods[0].name", is("Sodium")));
        mockMvc.perform(get("/api/modpacks/{id}/build", savedModpack.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods[0].name", is("Sodium")));
    }

    @Test
    void addModToModpack_duplicateReturns409AndLeavesOneEntry() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());

        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}", savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isConflict());

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(jsonPath("$.mods", hasSize(1)));
    }

    @Test
    void removeModFromModpack_staysRemovedAndKeepsTheMod() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());

        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}", savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods", hasSize(0)));

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(jsonPath("$.mods", hasSize(0)));
        mockMvc.perform(get("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isOk());
    }

    @Test
    void deleteModpack_keepsItsModsAndReleasesThem() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());

        mockMvc.perform(delete("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isNoContent());
    }

    /**
     * Pins the documented failure tracked in issue #14, whose fix is awaiting a decision between
     * cascading and a {@code 409}. Expect to replace this test with that fix. MockMvc has no error
     * dispatch, so the exception that production turns into a {@code 500} surfaces here.
     */
    @Test
    void deleteMod_stillInModpackFailsAndChangesNothing() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());

        assertThatThrownBy(() -> mockMvc.perform(delete("/api/mods/{id}", savedMod.getId())))
            .isInstanceOf(ServletException.class)
            .hasCauseInstanceOf(DataIntegrityViolationException.class);

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(jsonPath("$.mods", hasSize(1)))
            .andExpect(jsonPath("$.mods[0].name", is("Sodium")));
    }

    @Test
    void deleteMod_succeedsOnceRemovedFromEveryModpack() throws Exception {
        Modpack secondModpack = modpackBuilderService.createModpack(new Modpack("Second Pack", null, "1.20.1"));
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        modpackBuilderService.addModToModpack(secondModpack.getId(), savedMod.getId());

        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}", savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}", secondModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateMod_isSeenThroughItsModpack() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        Mod update = new Mod("Sodium", "0.6.0", "jellysquid", "Renderer", "http://example.com/sodium.jar", "1.20.2");

        mockMvc.perform(put("/api/mods/{id}", savedMod.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(jsonPath("$.mods[0].version", is("0.6.0")));
    }

    @Test
    void updateModpack_keepsItsMods() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());

        mockMvc.perform(put("/api/modpacks/{id}", savedModpack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Renamed Pack\",\"minecraftVersion\":\"1.20.2\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(jsonPath("$.name", is("Renamed Pack")))
            .andExpect(jsonPath("$.mods", hasSize(1)));
    }

    @Test
    void createMod_withExistingIdAddsARowInsteadOfOverwriting() throws Exception {
        Mod sameId = new Mod("Lithium", "0.11.2", "jellysquid", "Optimisations", "http://example.com/lithium.jar", "1.20.1");
        sameId.setId(savedMod.getId());

        mockMvc.perform(post("/api/mods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sameId)))
            .andExpect(status().isCreated());

        assertThat(modRepository.count()).isEqualTo(2);
        mockMvc.perform(get("/api/mods/{id}", savedMod.getId()))
            .andExpect(jsonPath("$.name", is("Sodium")));
    }
}
