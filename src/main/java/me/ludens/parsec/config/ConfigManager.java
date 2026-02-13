package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.input.KeybindManager;
import me.ludens.parsec.parsec;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Config manager that serializes keybinds and module data.
 * Uses reflection to support multiple KeyBinding/InputUtil APIs across mappings.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("parsec.json").toFile();

    public static void save() {
        try {
            ConfigData data = new ConfigData();

            // Save GUI keybind (resilient)
            try {
                Integer guiCode = getKeyCodeFromKeyBinding(KeybindManager.GUI_KEY);
                if (guiCode != null) data.guiKeybind = guiCode;
            } catch (Exception ignored) {}

            // Save module data
            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.enabled;
                config.x = module.x;
                config.y = module.y;
                config.backgroundColor = module.backgroundColor;

                try {
                    Integer code = getKeyCodeFromKeyBinding(module.keyBinding);
                    config.keybind = (code != null) ? code : GLFW.GLFW_KEY_UNKNOWN;
                } catch (Exception e) {
                    config.keybind = GLFW.GLFW_KEY_UNKNOWN;
                }

                data.modules.put(module.name, config);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(data, writer);
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
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) return;

            // Load GUI keybind (resilient)
            if (data.guiKeybind != 0) {
                try {
                    Object keyObj = buildKeyInputFromCode(data.guiKeybind);
                    setKeyBindingKey(KeybindManager.GUI_KEY, keyObj);
                } catch (Exception e) {
                    parsec.LOGGER.warn("Failed to restore GUI keybind", e);
                }
            }

            // Load module data
            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = data.modules.get(module.name);
                if (config != null) {
                    module.enabled = config.enabled;
                    module.x = config.x;
                    module.y = config.y;
                    module.backgroundColor = config.backgroundColor;

                    if (config.keybind != 0 && module.keyBinding != null) {
                        try {
                            Object keyObj = buildKeyInputFromCode(config.keybind);
                            setKeyBindingKey(module.keyBinding, keyObj);
                        } catch (Exception e) {
                            parsec.LOGGER.warn("Failed to restore keybind for module " + module.name, e);
                        }
                    }
                }
            }

            parsec.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            parsec.LOGGER.error("Failed to load config", e);
        }
    }

    // Try to extract an integer key code from a KeyBinding using multiple possible APIs
    private static Integer getKeyCodeFromKeyBinding(KeyBinding kb) {
        if (kb == null) return null;
        try {
            // Try modern API: getBoundKey().getCode()
            Method getBoundKey = kb.getClass().getMethod("getBoundKey");
            Object boundKey = getBoundKey.invoke(kb);
            if (boundKey != null) {
                Method getCode = boundKey.getClass().getMethod("getCode");
                Object codeObj = getCode.invoke(boundKey);
                if (codeObj instanceof Integer) return (Integer) codeObj;
            }
        } catch (NoSuchMethodException ignored) {
            // fallthrough to older API
        } catch (IllegalAccessException | InvocationTargetException e) {
            parsec.LOGGER.warn("Reflection error reading getBoundKey()", e);
        }

        try {
            // Try older API: getKeyInput().getCode()
            Method getKeyInput = kb.getClass().getMethod("getKeyInput");
            Object keyInput = getKeyInput.invoke(kb);
            if (keyInput != null) {
