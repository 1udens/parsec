package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.Parsec;
import me.ludens.parsec.input.KeybindManager;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles saving and loading configuration to/from JSON files.
 * UPDATED for Minecraft 1.21.11 API
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
     * FIXED for Minecraft 1.21.11 - uses defaultKey instead of getBoundKey()
     */
    public static Integer getKeyCode(KeyBinding kb) {
        if (kb == null) return null;
        
        try {
            // In 1.21.11, we access the default key directly
            return kb.getDefaultKey().getCode();
        } catch (Exception e) {
            Parsec.LOGGER.debug("Failed to get key code", e);
            return GLFW.GLFW_KEY_UNKNOWN;
        }
    }

    /**
     * Set a key code for a KeyBinding.
     * FIXED for Minecraft 1.21.11 - uses setBoundKey with proper Key creation
     */
    public static void setKeyCode(KeyBinding kb, int code) {
        if (kb == null) return;
        
        try {
            // In 1.21.11, we need to create a Key from the code differently
            // Use the existing default key type
            var keyType = kb.getDefaultKey().getCategory();
            var key = new net.minecraft.client.util.InputUtil.Key(keyType, code);
            kb.setBoundKey(key);
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to set key code", e);
        }
    }

    /**
     * Reset all settings to defaults.
     */
    public static void reset() {
        if (CONFIG_FILE.exists()) {
            CONFIG_FILE.delete();
            Parsec.LOGGER.info("Config file deleted");
        }
        
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            module.setEnabled(false);
            module.setBackgroundColor(0xAA000000);
            module.setTextColor(0xFFFFFFFF);
        }
        
        Parsec.LOGGER.info("Config reset to defaults");
    }

    private static class ConfigData {
        int guiKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT;
        Map<String, ModuleConfig> modules = new HashMap<>();
    }

    private static class ModuleConfig {
        boolean enabled;
        int x, y;
        int backgroundColor;
        int textColor;
        int keybind = GLFW.GLFW_KEY_UNKNOWN;
    }
}
