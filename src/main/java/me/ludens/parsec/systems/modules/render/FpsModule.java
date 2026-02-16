package me.ludens.parsec.systems.modules.render;

import me.ludens.parsec.systems.Category;
import me.ludens.parsec.systems.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Displays the current FPS (Frames Per Second) on the HUD.
 * 
 * Learning Note: This extends HudModule, inheriting all its functionality.
 * We only need to implement the render() method to define what it displays.
 */
public class FpsModule extends HudModule {
    
    public FpsModule() {
        super(
            "FPS",                          // Module name
            "Displays current FPS",         // Description
            Category.RENDER,                // Category
            10,                             // Initial X position
            10                              // Initial Y position
        );
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        // Get current FPS from Minecraft
        int fps = MinecraftClient.getInstance().getCurrentFps();
        
        // Format the display text
        String text = fps + " FPS";
        
        // Calculate width for background
        int width = textRenderer.getWidth(text);
        
        // Draw semi-transparent background
        drawBackground(drawContext, width);
        
        // Draw the text
        // Parameters: renderer, text, x, y, color (ARGB), shadow (true/false)
        drawContext.drawText(textRenderer, text, x, y, textColor, true);
    }

    /**
     * Optional: Add color coding based on FPS
     * Good FPS = Green, Medium = Yellow, Low = Red
     */
    private int getFpsColor(int fps) {
        if (fps >= 60) return 0xFF00FF00;      // Green
        if (fps >= 30) return 0xFFFFFF00;      // Yellow
        return 0xFFFF0000;                      // Red
    }

    /**
     * Example of using the color coding
     * Uncomment in render() to use: drawContext.drawText(textRenderer, text, x, y, getFpsColor(fps), true);
     */
}
