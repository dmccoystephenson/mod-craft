package com.modcraft.dto;

import jakarta.validation.constraints.NotBlank;

public class ModpackRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String minecraftVersion;

    public ModpackRequest() {}

    public ModpackRequest(String name, String description, String minecraftVersion) {
        this.name = name;
        this.description = description;
        this.minecraftVersion = minecraftVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }
}
