package me.ludens.parsec.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ludens.parsec.input.KeybindManager;
import me.ludens.parsec.parsec;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

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
            ConfigData data = new ConfigData();

            // Save GUI keybind
            data.guiKeybind = KeybindManager.GUI_KEY.getKeyInput().getCode();

            // Save module data
            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = new ModuleConfig();
                config.enabled = module.enabled;
                config.x = module.x;
                config.y = module.y;
                config.backgroundColor = module.backgroundColor;
                config.keybind = module.keyBinding.getKeyInput().getCode();
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

            // Load GUI keybind
            if (data.guiKeybind != 0) {
                KeybindManager.GUI_KEY.setKeyInput(InputUtil.fromKeyCode(data.guiKeybind));
            }

            // Load module data
            for (HudModule module : ModuleRenderer.modules) {
                ModuleConfig config = data.modules.get(module.name);
                if (config != null) {
                    module.enabled = config.enabled;
                    module.x = config.x;
                    module.y = config.y;
                    module.backgroundColor = config.backgroundColor;

                    if (config.keybind != 0) {
                        module.keyBinding.setKeyInput(InputUtil.fromKeyCode(config.keybind));
                    }
                }
            }
            parsec.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            parsec.LOGGER.error("Failed to load config", e);
        }
    }

    private static class ConfigData {
        int guiKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT;
        Map<String, ModuleConfig> modules = new HashMap<>();
    }

    private static class ModuleConfig {
        boolean enabled;
        int x, y;
        int backgroundColor;
        int keybind = GLFW.GLFW_KEY_UNKNOWN;
    }
}