package com.modcraft.exception;

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

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

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
            new Mod("JEI", "15.2.0", "mezz", "Item viewer", "http://example.com/jei.jar", "1.20.1")
        );
        savedModpack = modpackBuilderService.createModpack(
            new Modpack("Handler Pack", "Exercises error responses", "1.20.1")
        );
    }

    @Test
    void handleNotFound_missingMod_returnsErrorBody() throws Exception {
        mockMvc.perform(get("/api/mods/{id}", 987654L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", aMapWithSize(1)))
            .andExpect(jsonPath("$.error", is("Mod not found with id: 987654")));
    }

    @Test
    void handleNotFound_missingModpack_returnsErrorBody() throws Exception {
        mockMvc.perform(get("/api/modpacks/{id}/build", 987654L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", aMapWithSize(1)))
            .andExpect(jsonPath("$.error", is("Modpack not found with id: 987654")));
    }

    @Test
    void handleNotFound_modNotInModpack_returnsErrorBody() throws Exception {
        mockMvc.perform(delete("/api/modpacks/{modpackId}/mods/{modId}",
                savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$", aMapWithSize(1)))
            .andExpect(jsonPath("$.error", is(
                "Mod with id " + savedMod.getId() + " is not in modpack with id " + savedModpack.getId())));
    }

    @Test
    void handleConflict_duplicateMod_returnsErrorBody() throws Exception {
        modpackBuilderService.addModToModpack(savedModpack.getId(), savedMod.getId());
        mockMvc.perform(post("/api/modpacks/{modpackId}/mods/{modId}",
                savedModpack.getId(), savedMod.getId()))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", aMapWithSize(1)))
            .andExpect(jsonPath("$.error", is(
                "Mod with id " + savedMod.getId() + " is already in modpack with id " + savedModpack.getId())));
    }

    @Test
    void handleValidation_invalidMod_returnsFieldErrors() throws Exception {
        Mod invalid = new Mod("", "", "someone", "no required fields", null, "");
        mockMvc.perform(post("/api/mods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", aMapWithSize(3)))
            .andExpect(jsonPath("$.name", is("must not be blank")))
            .andExpect(jsonPath("$.version", is("must not be blank")))
            .andExpect(jsonPath("$.minecraftVersion", is("must not be blank")));
    }

    @Test
    void handleValidation_invalidModpackRequest_returnsOnlyFailingFields() throws Exception {
        ModpackRequest invalid = new ModpackRequest("", "description is optional", "1.20.1");
        mockMvc.perform(put("/api/modpacks/{id}", savedModpack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", aMapWithSize(1)))
            .andExpect(jsonPath("$.name", is("must not be blank")));
    }
}
