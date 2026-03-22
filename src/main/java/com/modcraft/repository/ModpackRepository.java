package com.modcraft.repository;

import com.modcraft.model.Modpack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModpackRepository extends JpaRepository<Modpack, Long> {

    List<Modpack> findByMinecraftVersion(String minecraftVersion);

    List<Modpack> findByNameContainingIgnoreCase(String name);
}
