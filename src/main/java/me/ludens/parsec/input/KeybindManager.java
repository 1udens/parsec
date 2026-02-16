package me.ludens.parsec.input;

import me.ludens.parsec.Parsec;
import me.ludens.parsec.gui.ClickGui;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Manages all keybindings for the mod.
 * 
 * Learning Note: Keybindings allow players to press keys to trigger actions.
 * We register them with Fabric's KeyBindingHelper, which handles the Minecraft
 * keybinding system for us.
 */
public class KeybindManager {
    public static KeyBinding GUI_KEY;

    /**
     * Register all keybindings.
     * Called once during mod initialization.
     */
    public static void register() {
        Parsec.LOGGER.info("Registering keybindings...");

        // Register GUI open keybind
        // Learning Note: "RIGHT_SHIFT" is the default key, but players can
        // change it in Minecraft's controls menu
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.parsec.open_gui",          // Translation key (from lang file)
                InputUtil.Type.KEYSYM,           // Key type
                GLFW.GLFW_KEY_RIGHT_SHIFT,      // Default key
                "key.categories.parsec"          // Category in controls menu
        ));

        // Register keybinds for each module
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            // Create translation key: "key.parsec.toggle_fps", etc.
            String translationKey = "key.parsec.toggle_" + 
                module.getName().toLowerCase().replace(" ", "_");
            
            KeyBinding moduleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    translationKey,
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,       // No default key (user must set)
                    "key.categories.parsec"
            ));
            
            module.setKeyBinding(moduleKey);
        }

        // Register tick event to check for key presses
        // Learning Note: ClientTickEvents.END_CLIENT_TICK happens every game tick
        // (20 times per second). We check if keys were pressed during that tick.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check GUI keybind
            while (GUI_KEY.wasPressed()) {
                client.setScreen(new ClickGui());
            }

            // Check module keybinds
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                if (module.getKeyBinding() != null) {
                    while (module.getKeyBinding().wasPressed()) {
                        module.toggle();
                        
                        // Log to game log (F3 + T to see)
                        Parsec.LOGGER.info("Toggled {} {}", 
                            module.getName(), 
                            module.isEnabled() ? "ON" : "OFF");
                    }
                }
            }
        });

        Parsec.LOGGER.info("Registered {} keybindings", 
            ModuleManager.INSTANCE.getModules().size() + 1);
    }

    /**
     * Get the GLFW key code for a keybinding.
     * Returns GLFW_KEY_UNKNOWN if the key cannot be determined.
     */
    public static int getKeyCode(KeyBinding keyBinding) {
        if (keyBinding == null) return GLFW.GLFW_KEY_UNKNOWN;
        
        try {
            return keyBinding.getBoundKey().getCode();
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to get key code for keybinding", e);
            return GLFW.GLFW_KEY_UNKNOWN;
        }
    }

    /**
     * Set a new key code for a keybinding.
     */
    public static void setKeyCode(KeyBinding keyBinding, int keyCode) {
        if (keyBinding == null) return;
        
        try {
            InputUtil.Key key = InputUtil.fromKeyCode(keyCode, -1);
            keyBinding.setBoundKey(key);
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to set key code for keybinding", e);
        }
    }
}
