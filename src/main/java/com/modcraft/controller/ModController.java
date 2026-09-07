package com.modcraft.controller;

import com.modcraft.model.Mod;
import com.modcraft.service.ModService;
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
@RequestMapping("/api/mods")
public class ModController {

    private final ModService modService;

    public ModController(ModService modService) {
        this.modService = modService;
    }

    @GetMapping
    public ResponseEntity<List<Mod>> getAllMods(
            @RequestParam(required = false) String minecraftVersion,
            @RequestParam(required = false) String search) {
        if (minecraftVersion != null) {
            return ResponseEntity.ok(modService.getModsByMinecraftVersion(minecraftVersion));
        }
        if (search != null) {
            return ResponseEntity.ok(modService.searchModsByName(search));
        }
        return ResponseEntity.ok(modService.getAllMods());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mod> getModById(@PathVariable Long id) {
        return ResponseEntity.ok(modService.getModById(id));
    }

    @PostMapping
    public ResponseEntity<Mod> createMod(@Valid @RequestBody Mod mod) {
        Mod created = modService.createMod(mod);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mod> updateMod(@PathVariable Long id, @Valid @RequestBody Mod mod) {
        return ResponseEntity.ok(modService.updateMod(id, mod));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMod(@PathVariable Long id) {
        modService.deleteMod(id);
        return ResponseEntity.noContent().build();
    }
}
