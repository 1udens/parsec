package me.ludens.parsec.systems;

import me.ludens.parsec.utils.Keybind;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for all HUD modules.
 * UPDATED to use the new Keybind system (similar to Meteor Client)
 * 
 * Learning Note: This now uses our custom Keybind class instead of
 * Minecraft's KeyBinding, giving us more flexibility and control.
 */
public abstract class HudModule {
    protected final String name;
    protected final String description;
    protected final Category category;
    
    private boolean enabled = false;
    
    protected int x;
    protected int y;
    
    protected int backgroundColor = 0xAA000000;
    protected int textColor = 0xFFFFFFFF;
    
    // NEW: Use our custom Keybind instead of Minecraft's KeyBinding
    protected Keybind keybind = new Keybind();

    public HudModule(String name, String description, Category category, int x, int y) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.x = x;
        this.y = y;
    }

    /**
     * Render the module on screen
     */
    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);

    /**
     * Called when the module is enabled
     */
    public void onEnable() {
        // Override in subclasses if needed
    }

    /**
     * Called when the module is disabled
     */
    public void onDisable() {
        // Override in subclasses if needed
    }

    /**
     * Toggle the module on/off
     */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Enable or disable the module
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        
        this.enabled = enabled;
        
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Check if the given input matches this module's keybind
     * 
     * Learning Note: This is similar to Meteor's Macro.onAction()
     * It checks if the key/button press matches our keybind.
     */
    public boolean matchesKeybind(boolean isKey, int value, int modifiers) {
        return keybind.matches(isKey, value, modifiers);
    }

    /**
     * Update the background color from hex and alpha values
     */
    public void updateColor(String hex, int alpha) {
        try {
            int rgb = Integer.parseInt(hex.replace("#", ""), 16);
            this.backgroundColor = (alpha << 24) | (rgb & 0xFFFFFF);
        } catch (NumberFormatException e) {
            // Invalid hex color, ignore
        }
    }

    /**
     * Draw a background rectangle behind text
     */
    protected void drawBackground(DrawContext drawContext, int width) {
        drawBackground(drawContext, width, 10);
    }

    protected void drawBackground(DrawContext drawContext, int width, int height) {
        drawContext.fill(
            x - 5,
            y - 5,
            x + width + 5,
            y + height + 5,
            backgroundColor
        );
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    // NEW: Keybind getter/setter
    public Keybind getKeybind() {
        return keybind;
    }

    public void setKeybind(Keybind keybind) {
        this.keybind = keybind;
    }
}
