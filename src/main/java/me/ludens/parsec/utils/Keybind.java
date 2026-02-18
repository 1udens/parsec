package me.ludens.parsec.utils;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Represents a keybind with support for keyboard keys, mouse buttons, and modifiers.
 * Similar to Meteor Client's Keybind system.
 * 
 * Learning Note: This class encapsulates all the logic for checking if a key
 * combination matches. It's much more flexible than directly using KeyBinding.
 */
public class Keybind {
    private boolean isKey;        // true for keyboard, false for mouse
    private int value;            // The key/button code
    private int modifiers;        // Modifier keys (Shift, Ctrl, Alt, etc.)

    /**
     * Creates an empty/unbound keybind
     */
    public Keybind() {
        this.isKey = true;
        this.value = GLFW.GLFW_KEY_UNKNOWN;
        this.modifiers = 0;
    }

    /**
     * Creates a keybind with specified key/button and modifiers
     * 
     * @param isKey - true for keyboard key, false for mouse button
     * @param value - The GLFW key or button code
     * @param modifiers - Modifier flags (GLFW_MOD_SHIFT, etc.)
     */
    public Keybind(boolean isKey, int value, int modifiers) {
        this.isKey = isKey;
        this.value = value;
        this.modifiers = modifiers;
    }

    /**
     * Creates a keybind from a keyboard key (no modifiers)
     */
    public static Keybind fromKey(int key) {
        return new Keybind(true, key, 0);
    }

    /**
     * Creates a keybind from a mouse button (no modifiers)
     */
    public static Keybind fromButton(int button) {
        return new Keybind(false, button, 0);
    }

    /**
     * Check if this keybind matches the given input
     * 
     * Learning Note: This is the core matching logic. It checks:
     * 1. Is it the right type (key vs button)?
     * 2. Is it the right key/button code?
     * 3. Do the modifiers match?
     */
    public boolean matches(boolean isKey, int value, int modifiers) {
        // If keybind is not set, it doesn't match anything
        if (this.value == GLFW.GLFW_KEY_UNKNOWN || this.value == -1) {
            return false;
        }

        // Check if input type matches (key vs button)
        if (this.isKey != isKey) return false;

        // Check if the key/button code matches
        if (this.value != value) return false;

        // Check if modifiers match
        // Learning Note: We use bitwise AND to check if required modifiers are pressed
        return (modifiers & this.modifiers) == this.modifiers;
    }

    /**
     * Check if this keybind is currently pressed
     */
    public boolean isPressed() {
        if (value == GLFW.GLFW_KEY_UNKNOWN || value == -1) return false;

        if (isKey) {
            return InputUtil.isKeyPressed(
                net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                value
            );
        } else {
            return GLFW.glfwGetMouseButton(
                net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                value
            ) == GLFW.GLFW_PRESS;
        }
    }

    /**
     * Get a human-readable name for this keybind
     * 
     * Learning Note: This creates strings like "Shift + F" or "Mouse 4"
     */
    public String getName() {
        if (value == GLFW.GLFW_KEY_UNKNOWN || value == -1) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();

        // Add modifier names
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) sb.append("Ctrl + ");
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) sb.append("Shift + ");
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) sb.append("Alt + ");
        if ((modifiers & GLFW.GLFW_MOD_SUPER) != 0) sb.append("Super + ");

        // Add key/button name
        if (isKey) {
            String keyName = GLFW.glfwGetKeyName(value, 0);
            if (keyName != null) {
                sb.append(keyName.toUpperCase());
            } else {
                sb.append(getSpecialKeyName(value));
            }
        } else {
            sb.append("Mouse ").append(value + 1);
        }

        return sb.toString();
    }

    /**
     * Get names for special keys that don't have printable names
     */
    private String getSpecialKeyName(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_ESCAPE -> "Escape";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> "Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> "Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> "Alt";
            default -> "Unknown";
        };
    }

    /**
     * Set this keybind from user input
     */
    public void set(boolean isKey, int value, int modifiers) {
        this.isKey = isKey;
        this.value = value;
        this.modifiers = modifiers;
    }

    /**
     * Clear this keybind (unbind)
     */
    public void clear() {
        this.value = GLFW.GLFW_KEY_UNKNOWN;
        this.modifiers = 0;
    }

    /**
     * Check if this keybind is set (not empty)
     */
    public boolean isSet() {
        return value != GLFW.GLFW_KEY_UNKNOWN && value != -1;
    }

    // Getters and setters
    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean key) {
        isKey = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keybind keybind = (Keybind) o;
        return isKey == keybind.isKey && 
               value == keybind.value && 
               modifiers == keybind.modifiers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isKey, value, modifiers);
    }

    @Override
    public String toString() {
        return getName();
    }
}
