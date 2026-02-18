package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.Parsec;
import me.ludens.parsec.input.InputHandler;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import me.ludens.parsec.utils.Keybind;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles saving and loading configuration.
 * UPDATED to work with the new Keybind system (similar to Meteor Client)
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("parsec.json").toFile();

    public static void save() {
        try {
            ConfigData data = new ConfigData();

            // Save GUI keybind
            Keybind guiKeybind = InputHandler.getGuiKeybind();
            data.guiKeybind = new KeybindData(guiKeybind);

            // Save each module's settings
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.isEnabled();
                config.x = module.getX();
                config.y = module.getY();
                config.backgroundColor = module.getBackgroundColor();
                config.textColor = module.getTextColor();
                
                // Save module keybind using our new system
                config.keybind = new KeybindData(module.getKeybind());

                data.modules.put(module.getName(), config);
            }

            // Ensure config directory exists
            if (!CONFIG_DIR.toFile().exists()) {
                CONFIG_DIR.toFile().mkdirs();
            }

            // Write to file
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
            }
            
            Parsec.LOGGER.info("Config saved successfully");
        } catch (Exception e) {
            Parsec.LOGGER.error("Failed to save config", e);
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            Parsec.LOGGER.info("No config file found, using defaults");
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) {
                Parsec.LOGGER.warn("Config file is empty or invalid");
                return;
            }

            // Load GUI keybind
            if (data.guiKeybind != null) {
                InputHandler.setGuiKeybind(
                    data.guiKeybind.isKey,
                    data.guiKeybind.value,
                    data.guiKeybind.modifiers
                );
            }

            // Load each module's settings
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                ModuleConfig config = data.modules.get(module.getName());
                if (config != null) {
                    module.setEnabled(config.enabled);
                    module.setX(config.x);
                    module.setY(config.y);
                    module.setBackgroundColor(config.backgroundColor);
                    module.setTextColor(config.textColor);

                    // Restore keybind
                    if (config.keybind != null) {
                        module.getKeybind().set(
                            config.keybind.isKey,
                            config.keybind.value,
                            config.keybind.modifiers
                        );
                    }
                }
            }

            Parsec.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            Parsec.LOGGER.error("Failed to load config", e);
        }
    }

    /**
     * Reset all settings to defaults
     */
    public static void reset() {
        if (CONFIG_FILE.exists()) {
            CONFIG_FILE.delete();
        }
        
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            module.setEnabled(false);
            module.setBackgroundColor(0xAA000000);
            module.setTextColor(0xFFFFFFFF);
            module.getKeybind().clear();
        }
        
        Parsec.LOGGER.info("Config reset to defaults");
    }

    /**
     * Data classes for JSON serialization
     * 
     * Learning Note: These simple POJOs (Plain Old Java Objects) are
     * automatically converted to/from JSON by GSON.
     */
    private static class ConfigData {
        KeybindData guiKeybind;
        Map<String, ModuleConfig> modules = new HashMap<>();
    }

    private static class ModuleConfig {
        boolean enabled;
        int x, y;
        int backgroundColor;
        int textColor;
        KeybindData keybind;
    }

    /**
     * Serializable keybind data
     * 
     * Learning Note: This class represents a keybind in a way that
     * can be easily saved to JSON and loaded back.
     */
    private static class KeybindData {
        boolean isKey;
        int value;
        int modifiers;

        // Default constructor for GSON
        public KeybindData() {}

        // Constructor from Keybind object
        public KeybindData(Keybind keybind) {
            this.isKey = keybind.isKey();
            this.value = keybind.getValue();
            this.modifiers = keybind.getModifiers();
        }
    }
}
