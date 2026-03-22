package com.modcraft.service;

import com.modcraft.exception.ResourceNotFoundException;
import com.modcraft.model.Mod;
import com.modcraft.repository.ModRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ModServiceTest {

    @Autowired
    private ModService modService;

    @Autowired
    private ModRepository modRepository;

    private Mod testMod;

    @BeforeEach
    void setUp() {
        testMod = modService.createMod(
            new Mod("JourneyMap", "5.9.7", "techbrew", "Mapping mod", "http://example.com/journeymap.jar", "1.20.1")
        );
    }

    @Test
    void createMod_persistsAndReturnsMod() {
        assertThat(testMod.getId()).isNotNull();
        assertThat(testMod.getName()).isEqualTo("JourneyMap");
        assertThat(testMod.getMinecraftVersion()).isEqualTo("1.20.1");
    }

    @Test
    void getAllMods_returnsSavedMods() {
        assertThat(modService.getAllMods()).isNotEmpty();
    }

    @Test
    void getModById_returnsCorrectMod() {
        Mod found = modService.getModById(testMod.getId());
        assertThat(found.getName()).isEqualTo("JourneyMap");
    }

    @Test
    void getModById_throwsWhenNotFound() {
        assertThatThrownBy(() -> modService.getModById(-1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMod_updatesFields() {
        Mod updates = new Mod("JourneyMap", "5.9.8", "techbrew", "Updated mapping mod",
            "http://example.com/journeymap-new.jar", "1.20.2");
        Mod updated = modService.updateMod(testMod.getId(), updates);
        assertThat(updated.getVersion()).isEqualTo("5.9.8");
        assertThat(updated.getMinecraftVersion()).isEqualTo("1.20.2");
    }

    @Test
    void deleteMod_removesMod() {
        Long id = testMod.getId();
        modService.deleteMod(id);
        assertThatThrownBy(() -> modService.getModById(id))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getModsByMinecraftVersion_filtersCorrectly() {
        modService.createMod(new Mod("OldMod", "1.0.0", "author", "desc", "url", "1.19.4"));
        assertThat(modService.getModsByMinecraftVersion("1.20.1")).hasSize(1);
        assertThat(modService.getModsByMinecraftVersion("1.19.4")).hasSize(1);
    }

    @Test
    void searchModsByName_findsByPartialName() {
        modService.createMod(new Mod("JourneyMap Extra", "1.0", "author", "desc", "url", "1.20.1"));
        assertThat(modService.searchModsByName("journey")).hasSize(2);
        assertThat(modService.searchModsByName("OptiFine")).isEmpty();
    }
}
