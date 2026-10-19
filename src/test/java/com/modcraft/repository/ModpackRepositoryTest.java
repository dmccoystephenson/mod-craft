package com.modcraft.repository;

import com.modcraft.model.Modpack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ModpackRepositoryTest {

    @Autowired
    private ModpackRepository modpackRepository;

    @BeforeEach
    void setUp() {
        modpackRepository.save(new Modpack("Repo Skyblock", "Island survival", "9.99.1"));
        modpackRepository.save(new Modpack("Repo Tech", "Automation", "9.99.1"));
        modpackRepository.save(new Modpack("Repo Magic", "Spells", "9.99"));
    }

    @Test
    void findByMinecraftVersion_returnsOnlyExactMatches() {
        assertThat(modpackRepository.findByMinecraftVersion("9.99.1"))
            .extracting(Modpack::getName)
            .containsExactlyInAnyOrder("Repo Skyblock", "Repo Tech");
    }

    @Test
    void findByMinecraftVersion_doesNotMatchPrefix() {
        assertThat(modpackRepository.findByMinecraftVersion("9.99"))
            .extracting(Modpack::getName)
            .containsExactly("Repo Magic");
    }

    @Test
    void findByMinecraftVersion_returnsEmptyWhenNoneMatch() {
        assertThat(modpackRepository.findByMinecraftVersion("0.0.0-none")).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_matchesSubstringRegardlessOfCase() {
        assertThat(modpackRepository.findByNameContainingIgnoreCase("rEPO t"))
            .extracting(Modpack::getName)
            .containsExactly("Repo Tech");
    }

    @Test
    void findByNameContainingIgnoreCase_matchesEveryModpackContainingTerm() {
        assertThat(modpackRepository.findByNameContainingIgnoreCase("repo "))
            .extracting(Modpack::getName)
            .containsExactlyInAnyOrder("Repo Skyblock", "Repo Tech", "Repo Magic");
    }

    @Test
    void findByNameContainingIgnoreCase_returnsEmptyWhenNoneMatch() {
        assertThat(modpackRepository.findByNameContainingIgnoreCase("no-such-modpack-name")).isEmpty();
    }
}
