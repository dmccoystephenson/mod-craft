package com.modcraft.service;

import com.modcraft.exception.ResourceNotFoundException;
import com.modcraft.model.Mod;
import com.modcraft.repository.ModRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ModService {

    private final ModRepository modRepository;

    public ModService(ModRepository modRepository) {
        this.modRepository = modRepository;
    }

    @Transactional(readOnly = true)
    public List<Mod> getAllMods() {
        return modRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mod getModById(Long id) {
        return modRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Mod not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Mod> getModsByMinecraftVersion(String minecraftVersion) {
        return modRepository.findByMinecraftVersion(minecraftVersion);
    }

    @Transactional(readOnly = true)
    public List<Mod> searchModsByName(String name) {
        return modRepository.findByNameContainingIgnoreCase(name);
    }

    public Mod createMod(Mod mod) {
        return modRepository.save(mod);
    }

    public Mod updateMod(Long id, Mod updatedMod) {
        Mod existing = getModById(id);
        existing.setName(updatedMod.getName());
        existing.setVersion(updatedMod.getVersion());
        existing.setAuthor(updatedMod.getAuthor());
        existing.setDescription(updatedMod.getDescription());
        existing.setFileUrl(updatedMod.getFileUrl());
        existing.setMinecraftVersion(updatedMod.getMinecraftVersion());
        return modRepository.save(existing);
    }

    public void deleteMod(Long id) {
        Mod mod = getModById(id);
        modRepository.delete(mod);
    }
}
