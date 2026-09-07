package com.modcraft.service;

import com.modcraft.exception.ModAlreadyInModpackException;
import com.modcraft.exception.ResourceNotFoundException;
import com.modcraft.model.Mod;
import com.modcraft.model.Modpack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ModpackBuilderServiceTest {

    @Autowired
    private ModpackBuilderService modpackBuilderService;

    @Autowired
    private ModService modService;

    private Mod testMod;
    private Modpack testModpack;

    @BeforeEach
    void setUp() {
        testMod = modService.createMod(
            new Mod("OptiFine", "1.0.0", "sp614x", "Performance mod", "http://example.com/optifine.jar", "1.20.1")
        );
        testModpack = modpackBuilderService.createModpack(
            new Modpack("My Pack", "A test modpack", "1.20.1")
        );
    }

    @Test
    void createModpack_persistsAndReturnsModpack() {
        assertThat(testModpack.getId()).isNotNull();
        assertThat(testModpack.getName()).isEqualTo("My Pack");
        assertThat(testModpack.getMinecraftVersion()).isEqualTo("1.20.1");
    }

    @Test
    void getAllModpacks_returnsSavedModpacks() {
        assertThat(modpackBuilderService.getAllModpacks()).isNotEmpty();
    }

    @Test
    void getModpackById_returnsCorrectModpack() {
        Modpack found = modpackBuilderService.getModpackById(testModpack.getId());
        assertThat(found.getName()).isEqualTo("My Pack");
    }

    @Test
    void getModpackById_throwsWhenNotFound() {
        assertThatThrownBy(() -> modpackBuilderService.getModpackById(-1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addModToModpack_addsModSuccessfully() {
        Modpack updated = modpackBuilderService.addModToModpack(testModpack.getId(), testMod.getId());
        assertThat(updated.getMods()).hasSize(1);
        assertThat(updated.getMods().get(0).getName()).isEqualTo("OptiFine");
    }

    @Test
    void addModToModpack_throwsWhenModAlreadyAdded() {
        modpackBuilderService.addModToModpack(testModpack.getId(), testMod.getId());
        assertThatThrownBy(() -> modpackBuilderService.addModToModpack(testModpack.getId(), testMod.getId()))
            .isInstanceOf(ModAlreadyInModpackException.class);
    }

    @Test
    void removeModFromModpack_removesModSuccessfully() {
        modpackBuilderService.addModToModpack(testModpack.getId(), testMod.getId());
        Modpack updated = modpackBuilderService.removeModFromModpack(testModpack.getId(), testMod.getId());
        assertThat(updated.getMods()).isEmpty();
    }

    @Test
    void removeModFromModpack_throwsWhenModNotInPack() {
        assertThatThrownBy(() -> modpackBuilderService.removeModFromModpack(testModpack.getId(), testMod.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void buildModpack_returnsAssembledModpack() {
        modpackBuilderService.addModToModpack(testModpack.getId(), testMod.getId());
        Modpack built = modpackBuilderService.buildModpack(testModpack.getId());
        assertThat(built.getMods()).hasSize(1);
    }

    @Test
    void updateModpack_updatesFields() {
        Modpack updates = new Modpack("Updated Pack", "New description", "1.20.2");
        Modpack updated = modpackBuilderService.updateModpack(testModpack.getId(), updates);
        assertThat(updated.getName()).isEqualTo("Updated Pack");
        assertThat(updated.getMinecraftVersion()).isEqualTo("1.20.2");
    }

    @Test
    void deleteModpack_removesModpack() {
        Long id = testModpack.getId();
        modpackBuilderService.deleteModpack(id);
        assertThatThrownBy(() -> modpackBuilderService.getModpackById(id))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getModpacksByMinecraftVersion_filtersCorrectly() {
        modpackBuilderService.createModpack(new Modpack("Other Pack", "desc", "1.19.4"));
        assertThat(modpackBuilderService.getModpacksByMinecraftVersion("1.20.1")).hasSize(1);
        assertThat(modpackBuilderService.getModpacksByMinecraftVersion("1.19.4")).hasSize(1);
    }
}
