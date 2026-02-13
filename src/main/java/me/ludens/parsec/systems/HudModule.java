package me.ludens.parsec.systems;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public abstract class HudModule {
    public String name;
    public boolean enabled;
    public int x, y;

    public int backgroundColor = 0xAA000000;
    public KeyBinding keyBinding;

    public HudModule(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.enabled = false;
    }

    public void updateColor(String hex, int alphaPercent) {
        try {
            String cleanHex = hex.replace("#", "");
            // Convert 0-100 alpha to 0-255 hex
            int alpha = (int) (alphaPercent * 2.55);
            // Parse the RGB hex
            int rgb = Integer.parseInt(cleanHex, 16);
            // Combine Alpha and RGB into ARGB format
            this.backgroundColor = (alpha << 24) | (rgb & 0x00FFFFFF);
        } catch (NumberFormatException e) {
            this.backgroundColor = 0xAA000000; // Fallback
        }
    }

    protected void drawBackground(DrawContext context, int width) {
        context.fill(x - 5, y - 5, x + width + 5, y + 12, this.backgroundColor);
    }

    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);
}