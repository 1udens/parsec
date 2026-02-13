package me.ludens.parsec.input;

import me.ludens.parsec.parsec;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    public static KeyBinding GUI_KEY;

    public static void register() {
        // Register GUI keybind
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.parsec.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        // Register module keybinds
        for (HudModule module : ModuleRenderer.modules) {
            module.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.parsec.toggle_" + module.name.toLowerCase().replace(" ", "_"),
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
                    KeyBinding.Category.MISC
            ));
        }

        // Register tick event to handle keypresses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle GUI key
            while (GUI_KEY.wasPressed()) {
                client.setScreen(new me.ludens.parsec.gui.ClickGui());
            }

            // Handle module toggle keys
            for (HudModule module : ModuleRenderer.modules) {
                while (module.keyBinding.wasPressed()) {
                    module.enabled = !module.enabled;
                    parsec.LOGGER.info("Toggled {} {}", module.name, module.enabled ? "ON" : "OFF");
                }
            }
        });
    }
}