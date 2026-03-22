package com.modcraft.service;

import com.modcraft.exception.ModAlreadyInModpackException;
import com.modcraft.exception.ResourceNotFoundException;
import com.modcraft.model.Mod;
import com.modcraft.model.Modpack;
import com.modcraft.repository.ModpackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ModpackBuilderService {

    private final ModpackRepository modpackRepository;
    private final ModService modService;

    public ModpackBuilderService(ModpackRepository modpackRepository, ModService modService) {
        this.modpackRepository = modpackRepository;
        this.modService = modService;
    }

    @Transactional(readOnly = true)
    public List<Modpack> getAllModpacks() {
        return modpackRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Modpack getModpackById(Long id) {
        return modpackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Modpack not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Modpack> getModpacksByMinecraftVersion(String minecraftVersion) {
        return modpackRepository.findByMinecraftVersion(minecraftVersion);
    }

    @Transactional(readOnly = true)
    public List<Modpack> searchModpacksByName(String name) {
        return modpackRepository.findByNameContainingIgnoreCase(name);
    }

    public Modpack createModpack(Modpack modpack) {
        return modpackRepository.save(modpack);
    }

    public Modpack updateModpack(Long id, Modpack updatedModpack) {
        Modpack existing = getModpackById(id);
        existing.setName(updatedModpack.getName());
        existing.setDescription(updatedModpack.getDescription());
        existing.setMinecraftVersion(updatedModpack.getMinecraftVersion());
        return modpackRepository.save(existing);
    }

    public void deleteModpack(Long id) {
        Modpack modpack = getModpackById(id);
        modpackRepository.delete(modpack);
    }

    public Modpack addModToModpack(Long modpackId, Long modId) {
        Modpack modpack = getModpackById(modpackId);
        Mod mod = modService.getModById(modId);

        boolean alreadyAdded = modpack.getMods().stream()
            .anyMatch(m -> m.getId().equals(modId));
        if (alreadyAdded) {
            throw new ModAlreadyInModpackException(
                "Mod with id " + modId + " is already in modpack with id " + modpackId);
        }

        modpack.getMods().add(mod);
        return modpackRepository.save(modpack);
    }

    public Modpack removeModFromModpack(Long modpackId, Long modId) {
        Modpack modpack = getModpackById(modpackId);
        modService.getModById(modId);

        boolean removed = modpack.getMods().removeIf(m -> m.getId().equals(modId));
        if (!removed) {
            throw new ResourceNotFoundException(
                "Mod with id " + modId + " is not in modpack with id " + modpackId);
        }

        return modpackRepository.save(modpack);
    }

    @Transactional(readOnly = true)
    public Modpack buildModpack(Long modpackId) {
        return getModpackById(modpackId);
    }
}
