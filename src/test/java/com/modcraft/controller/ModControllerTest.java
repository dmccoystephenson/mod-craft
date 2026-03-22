package com.modcraft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcraft.model.Mod;
import com.modcraft.service.ModService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class ModControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModService modService;

    private Mod savedMod;

    @BeforeEach
    void setUp() {
        savedMod = modService.createMod(
            new Mod("JourneyMap", "5.9.7", "techbrew", "Mapping mod", "http://example.com/jm.jar", "1.20.1")
        );
    }

    @Test
    void getAllMods_returns200() throws Exception {
        mockMvc.perform(get("/api/mods"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllMods_filterByMinecraftVersion() throws Exception {
        modService.createMod(new Mod("OldMod", "1.0", "author", "desc", "url", "1.19.4"));
        mockMvc.perform(get("/api/mods").param("minecraftVersion", "1.20.1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getModById_returns200() throws Exception {
        mockMvc.perform(get("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("JourneyMap")));
    }

    @Test
    void getModById_returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/mods/{id}", -1L))
            .andExpect(status().isNotFound());
    }

    @Test
    void createMod_returns201() throws Exception {
        Mod mod = new Mod("OptiFine", "1.0.0", "sp614x", "Performance mod", "http://url.com", "1.20.1");
        mockMvc.perform(post("/api/mods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mod)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("OptiFine")));
    }

    @Test
    void createMod_returns400WhenInvalid() throws Exception {
        Mod invalid = new Mod("", "", null, null, null, "");
        mockMvc.perform(post("/api/mods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateMod_returns200() throws Exception {
        Mod update = new Mod("JourneyMap", "6.0.0", "techbrew", "Updated", "http://new.url", "1.20.2");
        mockMvc.perform(put("/api/mods/{id}", savedMod.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version", is("6.0.0")));
    }

    @Test
    void deleteMod_returns204() throws Exception {
        mockMvc.perform(delete("/api/mods/{id}", savedMod.getId()))
            .andExpect(status().isNoContent());
    }
}
