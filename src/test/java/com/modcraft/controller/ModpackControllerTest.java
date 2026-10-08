package com.modcraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcraft.dto.ModpackRequest;
import com.modcraft.model.Mod;
import com.modcraft.model.Modpack;
import com.modcraft.service.ModpackBuilderService;
import com.modcraft.service.ModService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ModpackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModpackBuilderService modpackBuilderService;

    @Autowired
    private ModService modService;

    private Mod savedMod;
    private Modpack savedModpack;

    @BeforeEach
    void setUp() {
        savedMod = modService.createMod(
            new Mod("OptiFine", "1.0.0", "sp614x", "Performance mod", "http://example.com/optifine.jar", "1.20.1")
        );
        savedModpack = modpackBuilderService.createModpack(
            new Modpack("Test Pack", "A test modpack", "1.20.1")
        );
    }

    @Test
    void getAllModpacks_returns200() throws Exception {
        mockMvc.perform(get("/api/modpacks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllModpacks_filterByMinecraftVersion() throws Exception {
        modpackBuilderService.createModpack(new Modpack("Old Pack", "desc", "1.19.4"));
        mockMvc.perform(get("/api/modpacks").param("minecraftVersion", "1.19.4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Old Pack")));
    }

    @Test
    void getAllModpacks_searchByPartialName() throws Exception {
        modpackBuilderService.createModpack(new Modpack("Skyblock", "desc", "1.20.1"));
        mockMvc.perform(get("/api/modpacks").param("search", "SKY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Skyblock")));
    }

    @Test
    void getAllModpacks_minecraftVersionTakesPrecedenceOverSearch() throws Exception {
        modpackBuilderService.createModpack(new Modpack("Old Pack", "desc", "1.19.4"));
        mockMvc.perform(get("/api/modpacks").param("minecraftVersion", "1.20.1").param("search", "old"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Test Pack")));
    }

    @Test
    void getModpackById_returns200() throws Exception {
        mockMvc.perform(get("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Test Pack")));
    }

    @Test
    void getModpackById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/modpacks/{id}", -1L))
            .andExpect(status().isNotFound());
    }

    @Test
    void createModpack_returns201() throws Exception {
        ModpackRequest request = new ModpackRequest("New Pack", "desc", "1.20.1");
        mockMvc.perform(post("/api/modpacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("New Pack")));
    }

    @Test
    void createModpack_returns400WhenInvalid() throws Exception {
        ModpackRequest invalid = new ModpackRequest("", "", "");
        mockMvc.perform(post("/api/modpacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateModpack_returns200() throws Exception {
        ModpackRequest update = new ModpackRequest("Updated Pack", "new desc", "1.20.2");
        mockMvc.perform(put("/api/modpacks/{id}", savedModpack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Updated Pack")));
    }

    @Test
    void updateModpack_returns400WhenInvalid() throws Exception {
        ModpackRequest invalid = new ModpackRequest("Updated Pack", "new desc", "");
        mockMvc.perform(put("/api/modpacks/{id}", savedModpack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
        assertThat(modpackBuilderService.getModpackById(savedModpack.getId()).getName()).isEqualTo("Test Pack");
    }

    @Test
    void updateModpack_returns404WhenNotFound() throws Exception {
        ModpackRequest update = new ModpackRequest("Updated Pack", "new desc", "1.20.2");
        mockMvc.perform(put("/api/modpacks/{id}", -1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteModpack_returns204() throws Exception {
        mockMvc.perform(delete("/api/modpacks/{id}", savedModpack.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteModpack_returns404WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/modpacks/{id}", -1L))
            .andExpect(status().isNotFound());
    }

    @Test
    void addModToModpack_returns404WhenModpackNotFound() throws Exception {
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}", -1L, savedMod.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Modpack not found with id: -1")));
    }

    @Test
    void addModToModpack_returns404WhenModNotFound() throws Exception {
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}", savedModpack.getId(), -1L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Mod not found with id: -1")));
    }

    @Test
    void addModToModpack_returns200() throws Exception {
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}",
                savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods", hasSize(1)));
    }

    @Test
    void addModToModpack_returns409WhenDuplicate() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}",
                savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isConflict());
    }

    @Test
    void removeModFromModpack_returns200() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}",
                savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods", hasSize(0)));
    }

    @Test
    void removeModFromModpack_returns404WhenModpackNotFound() throws Exception {
        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}", -1L, savedMod.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("Modpack not found with id: -1")));
    }

    @Test
    void buildModpack_returns200WithMods() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        mockMvc.perform(get("/api/modpacks/{id}/build", savedModpack.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mods", hasSize(1)));
    }

    @Test
    void buildModpack_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/modpacks/{id}/build", -1L))
            .andExpect(status().isNotFound());
    }
}

