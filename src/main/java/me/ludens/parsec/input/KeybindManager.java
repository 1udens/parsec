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
 * UPDATED for Minecraft 1.21.11 API
 */
public class KeybindManager {
    public static KeyBinding GUI_KEY;

    public static void register() {
        Parsec.LOGGER.info("Registering keybindings...");

        // Register GUI open keybind
        // FIXED: Use KeyBinding.Category.MISC instead of string
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.parsec.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC.getTranslationKey()  // FIXED for 1.21.11
        ));

        // Register keybinds for each module
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            String translationKey = "key.parsec.toggle_" + 
                module.getName().toLowerCase().replace(" ", "_");
            
            KeyBinding moduleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    translationKey,
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    KeyBinding.Category.MISC.getTranslationKey()  // FIXED for 1.21.11
            ));
            
            module.setKeyBinding(moduleKey);
        }

        // Register tick event to check for key presses
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
     * FIXED for Minecraft 1.21.11
     */
    public static int getKeyCode(KeyBinding keyBinding) {
        if (keyBinding == null) return GLFW.GLFW_KEY_UNKNOWN;
        
        try {
            return keyBinding.getDefaultKey().getCode();
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to get key code for keybinding", e);
            return GLFW.GLFW_KEY_UNKNOWN;
        }
    }

    /**
     * Set a new key code for a keybinding.
     * FIXED for Minecraft 1.21.11
     */
    public static void setKeyCode(KeyBinding keyBinding, int keyCode) {
        if (keyBinding == null) return;
        
        try {
            var keyType = keyBinding.getDefaultKey().getCategory();
            var key = new InputUtil.Key(keyType, keyCode);
            keyBinding.setBoundKey(key);
        } catch (Exception e) {
            Parsec.LOGGER.warn("Failed to set key code for keybinding", e);
        }
    }
}
