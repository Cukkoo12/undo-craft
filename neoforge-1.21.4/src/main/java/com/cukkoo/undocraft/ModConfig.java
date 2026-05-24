package com.cukkoo.undocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("undo-craft.json");

    // Crafting Table Screen button offsets (relative to leftPos and topPos)
    public int craftingTableX = 121;
    public int craftingTableY = 58;

    // Survival Inventory Screen button offsets (relative to leftPos and topPos)
    public int inventoryX = 151;
    public int inventoryY = 50;

    private static ModConfig INSTANCE;

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                ModConfig cfg = new Gson().fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH,
                new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (Exception e) {
            // fallback
        }
    }
}
