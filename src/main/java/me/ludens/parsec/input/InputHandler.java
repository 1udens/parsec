package me.ludens.parsec.input;

import me.ludens.parsec.Parsec;
import me.ludens.parsec.events.KeyEvent;
import me.ludens.parsec.events.MouseButtonEvent;
import me.ludens.parsec.gui.ClickGui;
import me.ludens.parsec.systems.ModuleManager;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.utils.Keybind;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * Handles all keyboard and mouse input for the mod.
 * Similar to Meteor Client's input handling system.
 * 
 * Learning Note: This class acts as a central hub for input processing.
 * It receives key/mouse events and dispatches them to the appropriate modules.
 */
public class InputHandler {
    // GUI keybind - use our custom Keybind class
    private static final Keybind GUI_KEYBIND = Keybind.fromKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    
    // Track last pressed state to detect key presses
    private static boolean guiKeyWasPressed = false;

    /**
     * Initialize the input handler
     */
    public static void init() {
        Parsec.LOGGER.info("Initializing input handler...");
        
        // Register tick event to check keybinds
        // Learning Note: We check on tick rather than on key event because
        // tick events happen after all input processing, avoiding conflicts
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            checkKeybinds(client);
        });
        
        Parsec.LOGGER.info("Input handler initialized");
    }

    /**
     * Check all keybinds each tick
     * 
     * Learning Note: This is called 20 times per second (every tick).
     * We check if keybinds are pressed and trigger the appropriate actions.
     */
    private static void checkKeybinds(MinecraftClient client) {
        // Don't process keybinds if a screen is open (except GUI keybind)
        if (client.currentScreen != null && !(client.currentScreen instanceof ClickGui)) {
            guiKeyWasPressed = false;
            return;
        }

        // Check GUI keybind
        boolean guiKeyPressed = GUI_KEYBIND.isPressed();
        if (guiKeyPressed && !guiKeyWasPressed) {
            client.setScreen(new ClickGui());
        }
        guiKeyWasPressed = guiKeyPressed;

        // Check module keybinds (only if no screen is open)
        if (client.currentScreen == null) {
            for (HudModule module : ModuleManager.INSTANCE.getModules()) {
                if (module.getKeybind().isPressed()) {
                    module.toggle();
                    Parsec.LOGGER.info("Toggled {} {}", 
                        module.getName(), 
                        module.isEnabled() ? "ON" : "OFF");
                }
            }
        }
    }

    /**
     * Handle key press events
     * 
     * Learning Note: This would be called from a mixin that intercepts
     * keyboard input. Similar to Meteor's key event handling.
     * 
     * @return true if the event should be cancelled
     */
    public static boolean onKey(int key, int scancode, int action, int modifiers) {
        KeyEvent event = KeyEvent.get(key, scancode, action, modifiers);
        
        // Only process on press or repeat (not release)
        if (action == GLFW.GLFW_RELEASE) {
            return false;
        }

        // Check if any module wants to handle this key
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            if (module.matchesKeybind(true, key, modifiers)) {
                module.toggle();
                Parsec.LOGGER.info("Toggled {} {}", 
                    module.getName(), 
                    module.isEnabled() ? "ON" : "OFF");
                return true; // Cancel the event
            }
        }

        return event.isCancelled();
    }

    /**
     * Handle mouse button events
     * 
     * Learning Note: Similar to onKey but for mouse buttons.
     * Allows binding modules to mouse buttons like Mouse 4, Mouse 5, etc.
     */
    public static boolean onMouseButton(int button, int action, int modifiers) {
        MouseButtonEvent event = MouseButtonEvent.get(button, action, modifiers);
        
        if (action == GLFW.GLFW_RELEASE) {
            return false;
        }

        // Check if any module wants to handle this button
        for (HudModule module : ModuleManager.INSTANCE.getModules()) {
            if (module.matchesKeybind(false, button, modifiers)) {
                module.toggle();
                Parsec.LOGGER.info("Toggled {} {}", 
                    module.getName(), 
                    module.isEnabled() ? "ON" : "OFF");
                return true;
            }
        }

        return event.isCancelled();
    }

    /**
     * Get the GUI keybind for display/configuration
     */
    public static Keybind getGuiKeybind() {
        return GUI_KEYBIND;
    }

    /**
     * Set the GUI keybind
     */
    public static void setGuiKeybind(boolean isKey, int value, int modifiers) {
        GUI_KEYBIND.set(isKey, value, modifiers);
    }
}
