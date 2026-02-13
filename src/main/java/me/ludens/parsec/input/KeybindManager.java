package me.ludens.parsec.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Simple keybind manager for Parsec.
 * Adjust category and default key as you like.
 */
public final class KeybindManager {
    // Example GUI key (Right Shift by default)
    public static final KeyBinding GUI_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.parsec.gui",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_RIGHT_SHIFT,
                    "category.parsec"
            )
    );

    private KeybindManager() {}

    // Keep this method so existing code that calls KeybindManager.register() still works.
    public static void register() {
        // Registration is done at static init via KeyBindingHelper.registerKeyBinding.
        // This method exists for compatibility with older code.
    }
}
