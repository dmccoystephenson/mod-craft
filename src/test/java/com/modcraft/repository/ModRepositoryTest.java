package com.modcraft.repository;

import com.modcraft.model.Mod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ModRepositoryTest {

    @Autowired
    private ModRepository modRepository;

    @BeforeEach
    void setUp() {
        modRepository.save(new Mod("Repo Sodium", "0.5.3", "jellysquid", "Rendering", null, "9.99.1"));
        modRepository.save(new Mod("Repo Lithium", "0.11.2", "jellysquid", "Server optimisation", null, "9.99.1"));
        modRepository.save(new Mod("Repo Iris", "1.6.4", "coderbot", "Shaders", null, "9.99"));
    }

    @Test
    void findByMinecraftVersion_returnsOnlyExactMatches() {
        assertThat(modRepository.findByMinecraftVersion("9.99.1"))
            .extracting(Mod::getName)
            .containsExactlyInAnyOrder("Repo Sodium", "Repo Lithium");
    }

    @Test
    void findByMinecraftVersion_doesNotMatchPrefix() {
        assertThat(modRepository.findByMinecraftVersion("9.99"))
            .extracting(Mod::getName)
            .containsExactly("Repo Iris");
    }

    @Test
    void findByMinecraftVersion_returnsEmptyWhenNoneMatch() {
        assertThat(modRepository.findByMinecraftVersion("0.0.0-none")).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_matchesSubstringRegardlessOfCase() {
        assertThat(modRepository.findByNameContainingIgnoreCase("rEPO s"))
            .extracting(Mod::getName)
            .containsExactly("Repo Sodium");
    }

    @Test
    void findByNameContainingIgnoreCase_matchesEveryModContainingTerm() {
        assertThat(modRepository.findByNameContainingIgnoreCase("repo "))
            .extracting(Mod::getName)
            .containsExactlyInAnyOrder("Repo Sodium", "Repo Lithium", "Repo Iris");
    }

    @Test
    void findByNameContainingIgnoreCase_returnsEmptyWhenNoneMatch() {
        assertThat(modRepository.findByNameContainingIgnoreCase("no-such-mod-name")).isEmpty();
    }
}
