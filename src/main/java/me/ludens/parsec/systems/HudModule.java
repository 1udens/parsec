package me.ludens.parsec.systems;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/**
 * Base class for all HUD modules.
 * 
 * Learning Note: This is an abstract class, which means:
 * 1. It cannot be instantiated directly
 * 2. Subclasses MUST implement the abstract render() method
 * 3. It provides common functionality for all modules
 */
public abstract class HudModule {
    // Module properties
    protected final String name;
    protected final String description;
    protected final Category category;
    
    // State
    private boolean enabled = false;
    
    // Positioning
    protected int x;
    protected int y;
    
    // Styling
    protected int backgroundColor = 0xAA000000; // Semi-transparent black (ARGB format)
    protected int textColor = 0xFFFFFFFF;       // White
    
    // Keybinding
    public KeyBinding keyBinding;

    /**
     * Constructor for HudModule
     * 
     * @param name - Display name of the module
     * @param description - Brief description of what it does
     * @param category - Which category it belongs to
     * @param x - Initial X position on screen
     * @param y - Initial Y position on screen
     */
    public HudModule(String name, String description, Category category, int x, int y) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.x = x;
        this.y = y;
    }

    /**
     * This method is called every frame when the module is enabled.
     * Subclasses must implement this to draw their content.
     * 
     * Learning Note: The 'abstract' keyword means subclasses MUST provide
     * their own implementation of this method.
     */
    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);

    /**
     * Called when the module is enabled.
     * Override this in subclasses to add custom enable logic.
     */
    public void onEnable() {
        // Override in subclasses if needed
    }

    /**
     * Called when the module is disabled.
     * Override this in subclasses to add custom disable logic.
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
        if (this.enabled == enabled) return; // No change needed
        
        this.enabled = enabled;
        
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Update the background color from hex and alpha values
     * 
     * Learning Note: Colors in Java are often stored as ARGB (Alpha, Red, Green, Blue)
     * Each component is 1 byte (0-255), packed into a 32-bit integer.
     * Format: 0xAARRGGBB
     */
    public void updateColor(String hex, int alpha) {
        try {
            // Remove '#' if present and parse as hexadecimal
            int rgb = Integer.parseInt(hex.replace("#", ""), 16);
            // Combine alpha (shifted left 24 bits) with RGB
            this.backgroundColor = (alpha << 24) | (rgb & 0xFFFFFF);
        } catch (NumberFormatException e) {
            // Invalid hex color, ignore
        }
    }

    /**
     * Draw a background rectangle behind text
     * Uses default height of 10 pixels
     */
    protected void drawBackground(DrawContext drawContext, int width) {
        drawBackground(drawContext, width, 10);
    }

    /**
     * Draw a background rectangle behind text
     * 
     * @param drawContext - Minecraft's drawing context
     * @param width - Width of the text/content
     * @param height - Height of the background
     * 
     * Learning Note: The fill() method draws a filled rectangle.
     * We add/subtract 5 pixels for padding around the text.
     */
    protected void drawBackground(DrawContext drawContext, int width, int height) {
        drawContext.fill(
            x - 5,              // Left edge (with padding)
            y - 5,              // Top edge (with padding)
            x + width + 5,      // Right edge (with padding)
            y + height + 5,     // Bottom edge (with padding)
            backgroundColor     // Color (ARGB format)
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

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    public void setKeyBinding(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }
}
