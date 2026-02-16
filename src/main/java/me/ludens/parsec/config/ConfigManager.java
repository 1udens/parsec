package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.Parsec;
import me.ludens.parsec.input.KeybindManager;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles saving and loading configuration to/from JSON files.
 * 
 * Learning Note: GSON is a library that converts Java objects to JSON
 * and vice versa. This makes it easy to save configuration.
 */
public class ConfigManager {
    // GSON instance with pretty printing enabled
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    // File locations
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("parsec.json").toFile();

    /**
     * Save all module settings to config file.
     * 
     * Learning Note: This uses try-with-resources (try (...)) which automatically
     * closes the FileWriter when done, even if an error occurs.
     */
    public static void save() {
        try {
            ConfigData data = new ConfigData();

            // Save GUI keybind
            try {
                Integer code = getKeyCode(KeybindManager.GUI_KEY);
                if (code != null && code != GLFW.GLFW_KEY_UNKNOWN) {
                    data.guiKeybind = code;
                }
            } catch (Exception e) {
                Parsec.LOGGER.warn("Failed to save GUI keybind", e);
            }

            // Save each module's settings
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.isEnabled();
                config.x = module.getX();
                config.y = module.getY();
                config.backgroundColor = module.getBackgroundColor();
                config.textColor = module.getTextColor();

                // Save module keybind
                try {
                    Integer code = getKeyCode(module.getKeyBinding());
                    config.keybind = (code != null) ? code : GLFW.GLFW_KEY_UNKNOWN;
                } catch (Exception e) {
                    config.keybind = GLFW.GLFW_KEY_UNKNOWN;
                    Parsec.LOGGER.warn("Failed to save keybind for {}", module.getName(), e);
                }

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
            
            Parsec.LOGGER.info("Config saved successfully to {}", CONFIG_FILE.getPath());
        } catch (Exception e) {
            Parsec.LOGGER.error("Failed to save config", e);
        }
    }

    /**
     * Load all module settings from config file.
     */
    public static void load() {
        if (!CONFIG_FILE.exists()) {
            Parsec.LOGGER.info("No config file found at {}, using defaults", CONFIG_FILE.getPath());
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) {
                Parsec.LOGGER.warn("Config file is empty or invalid");
                return;
            }

            // Load GUI keybind
            if (data.guiKeybind != 0 && data.guiKeybind != GLFW.GLFW_KEY_UNKNOWN) {
                try {
                    setKeyCode(KeybindManager.GUI_KEY, data.guiKeybind);
                } catch (Exception e) {
                    Parsec.LOGGER.warn("Failed to restore GUI keybind", e);
                }
            }

            // Load each module's settings
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                ModuleConfig config = data.modules.get(module.getName());
                if (config != null) {
                    // Restore module state
                    module.setEnabled(config.enabled);
                    module.setX(config.x);
                    module.setY(config.y);
                    module.setBackgroundColor(config.backgroundColor);
                    module.setTextColor(config.textColor);

                    // Restore keybind
                    if (config.keybind != 0 && config.keybind != GLFW.GLFW_KEY_UNKNOWN) {
                        try {
                            setKeyCode(module.getKeyBinding(), config.keybind);
                        } catch (Exception e) {
                            Parsec.LOGGER.warn("Failed to restore keybind for {}", 
                                module.getName(), e);
                        }
                    }
                } else {
                    Parsec.LOGGER.debug("No config found for module: {}", module.getName());
                }
            }

            Parsec.LOGGER.info("Config loaded successfully from {}", CONFIG_FILE.getPath());
        } catch (FileNotFoundException e) {
            Parsec.LOGGER.warn("Config file not found: {}", CONFIG_FILE.getPath());
        } catch (Exception e) {
            Parsec.LOGGER.error("Failed to load config", e);
        }
    }

    /**
     * Get the key code from a KeyBinding.
     * 
     * Learning Note: We use reflection here because KeyBinding's internal
     * structure might vary between Minecraft versions.
     */
    public static Integer getKeyCode(KeyBinding kb) {
        if (kb == null) return null;
        
        try {
            return kb.getBoundKey().getCode();
        } catch (Exception e) {
            Parsec.LOGGER.debug("Failed to get key code", e);
            return GLFW.GLFW_KEY_UNKNOWN;
        }
    }

    /**
     * Set a key code for a KeyBinding.
     */
    public static void setKeyCode(KeyBinding kb, int code) {
        if (kb == null) return;
        
        try {
            InputUtil.Key key = InputUtil.fromKeyCode(code, -1);
            kb.setBoundKey(key);
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to set key code", e);
        }
    }

    /**
     * Reset all settings to defaults.
     * Useful for troubleshooting or starting fresh.
     */
    public static void reset() {
        if (CONFIG_FILE.exists()) {
            CONFIG_FILE.delete();
            Parsec.LOGGER.info("Config file deleted");
        }
        
        // Disable all modules and reset positions
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            module.setEnabled(false);
            module.setBackgroundColor(0xAA000000);
            module.setTextColor(0xFFFFFFFF);
        }
        
        Parsec.LOGGER.info("Config reset to defaults");
    }

    /**
     * Inner class representing the entire config file structure.
     * 
     * Learning Note: This is a POJO (Plain Old Java Object) that GSON
     * will serialize to JSON automatically.
     */
    private static class ConfigData {
        int guiKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT;
        Map<String, ModuleConfig> modules = new HashMap<>();
    }

    /**
     * Inner class representing a single module's configuration.
     */
    private static class ModuleConfig {
        boolean enabled;
        int x, y;
        int backgroundColor;
        int textColor;
        int keybind = GLFW.GLFW_KEY_UNKNOWN;
    }
}
