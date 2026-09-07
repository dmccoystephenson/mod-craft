package com.modcraft.repository;

import com.modcraft.model.Mod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModRepository extends JpaRepository<Mod, Long> {

    List<Mod> findByMinecraftVersion(String minecraftVersion);

    List<Mod> findByNameContainingIgnoreCase(String name);
}
