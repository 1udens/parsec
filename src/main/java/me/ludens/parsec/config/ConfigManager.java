package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.parsec;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("parsec.json").toFile();

    public static void save() {
        try {
            Map<String, ModuleConfig> configMap = new HashMap<>();

            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.enabled;
                config.x = module.x;
                config.y = module.y;
                config.backgroundColor = module.backgroundColor;
                configMap.put(module.name, config);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(configMap, writer);
            }
            parsec.LOGGER.info("Config saved successfully");
        } catch (Exception e) {
            parsec.LOGGER.error("Failed to save config", e);
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            parsec.LOGGER.info("No config file found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            @SuppressWarnings("unchecked")
            Map<String, ModuleConfig> configMap = GSON.fromJson(reader, Map.class);

            if (configMap == null) return;

            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = configMap.get(module.name);
                if (config != null) {
                    module.enabled = config.enabled;
                    module.x = config.x;
                    module.y = config.y;
                    module.backgroundColor = config.backgroundColor;
                }
            }
            parsec.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            parsec.LOGGER.error("Failed to load config", e);
        }
    }

    private static class ModuleConfig {
        boolean enabled;
        int x, y;
        int backgroundColor;
    }
}