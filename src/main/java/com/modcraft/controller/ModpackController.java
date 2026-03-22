package com.modcraft.controller;

import com.modcraft.model.Modpack;
import com.modcraft.service.ModpackBuilderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/modpacks")
public class ModpackController {

    private final ModpackBuilderService modpackBuilderService;

    public ModpackController(ModpackBuilderService modpackBuilderService) {
        this.modpackBuilderService = modpackBuilderService;
    }

    @GetMapping
    public ResponseEntity<List<Modpack>> getAllModpacks(
            @RequestParam(required = false) String minecraftVersion,
            @RequestParam(required = false) String search) {
        if (minecraftVersion != null) {
            return ResponseEntity.ok(modpackBuilderService.getModpacksByMinecraftVersion(minecraftVersion));
        }
        if (search != null) {
            return ResponseEntity.ok(modpackBuilderService.searchModpacksByName(search));
        }
        return ResponseEntity.ok(modpackBuilderService.getAllModpacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Modpack> getModpackById(@PathVariable Long id) {
        return ResponseEntity.ok(modpackBuilderService.getModpackById(id));
    }

    @PostMapping
    public ResponseEntity<Modpack> createModpack(@Valid @RequestBody Modpack modpack) {
        Modpack created = modpackBuilderService.createModpack(modpack);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Modpack> updateModpack(@PathVariable Long id, @Valid @RequestBody Modpack modpack) {
        return ResponseEntity.ok(modpackBuilderService.updateModpack(id, modpack));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModpack(@PathVariable Long id) {
        modpackBuilderService.deleteModpack(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{modpackId}/mods/{modId}")
    public ResponseEntity<Modpack> addModToModpack(
            @PathVariable Long modpackId,
            @PathVariable Long modId) {
        return ResponseEntity.ok(modpackBuilderService.addModToModpack(modpackId, modId));
    }

    @DeleteMapping("/{modpackId}/mods/{modId}")
    public ResponseEntity<Modpack> removeModFromModpack(
            @PathVariable Long modpackId,
            @PathVariable Long modId) {
        return ResponseEntity.ok(modpackBuilderService.removeModFromModpack(modpackId, modId));
    }

    @GetMapping("/{id}/build")
    public ResponseEntity<Modpack> buildModpack(@PathVariable Long id) {
        return ResponseEntity.ok(modpackBuilderService.buildModpack(id));
    }
}
