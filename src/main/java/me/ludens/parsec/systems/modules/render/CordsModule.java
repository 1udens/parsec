package me.ludens.parsec.systems.modules.render;

import me.ludens.parsec.systems.Category;
import me.ludens.parsec.systems.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Displays the player's current coordinates on the HUD.
 * 
 * Learning Note: This module uses caching to avoid recreating the
 * text string every frame when the position hasn't changed.
 * This is a performance optimization.
 */
public class CordsModule extends HudModule {
    // Cache for performance - only update when position changes
    private String cachedText = "";
    private int lastX = Integer.MAX_VALUE;
    private int lastY = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;

    public CordsModule() {
        super(
            "Coordinates",                      // Module name
            "Displays your XYZ coordinates",    // Description
            Category.RENDER,                    // Category
            10,                                 // Initial X position
            25                                  // Initial Y position (below FPS)
        );
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Safety check - don't render if player doesn't exist
        if (client.player == null) return;

        // Get player's current position
        // Learning Note: We cast to int to round down to block coordinates
        int xPos = (int) client.player.getX();
        int yPos = (int) client.player.getY();
        int zPos = (int) client.player.getZ();

        // Only update text if position changed
        // This avoids creating a new String object every frame
        if (xPos != lastX || yPos != lastY || zPos != lastZ) {
            cachedText = String.format("XYZ: %d, %d, %d", xPos, yPos, zPos);
            lastX = xPos;
            lastY = yPos;
            lastZ = zPos;
        }

        // Calculate width and draw
        int width = textRenderer.getWidth(cachedText);
        drawBackground(drawContext, width);
        drawContext.drawText(textRenderer, cachedText, x, y, textColor, true);
    }

    @Override
    public void onDisable() {
        // Clear cache when disabled to save memory
        cachedText = "";
        lastX = Integer.MAX_VALUE;
        lastY = Integer.MAX_VALUE;
        lastZ = Integer.MAX_VALUE;
    }

    /**
     * Alternative formatting options you can try:
     * 
     * Nether coordinates (divide by 8):
     * String.format("Overworld: %d, %d, %d | Nether: %d, %d, %d", 
     *               xPos, yPos, zPos, xPos/8, yPos, zPos/8)
     * 
     * Decimal precision:
     * String.format("X: %.1f Y: %.1f Z: %.1f", 
     *               client.player.getX(), client.player.getY(), client.player.getZ())
     * 
     * Separate lines:
     * String text1 = "X: " + xPos;
     * String text2 = "Y: " + yPos;
     * String text3 = "Z: " + zPos;
     * (Would need to adjust rendering code)
     */
}
