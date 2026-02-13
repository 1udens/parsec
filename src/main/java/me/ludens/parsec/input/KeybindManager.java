package me.ludens.parsec.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    public static KeyBinding GUI_KEY;

    public static void register() {
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.parsec.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.parsec.main"
        ));
    }
}