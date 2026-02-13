package me.ludens.parsec.systems;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding; // Added

public abstract class HudModule {
    public String name;
    public boolean enabled = false;
    public int x, y;
    public int backgroundColor = 0xAA000000;
    public KeyBinding keyBinding; // Added for ConfigManager

    public HudModule(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);

    public void updateColor(String hex, int alpha) {
        try {
            int rgb = Integer.parseInt(hex.replace("#", ""), 16);
            this.backgroundColor = (alpha << 24) | (rgb & 0xFFFFFF);
        } catch (NumberFormatException ignored) {}
    }

    // Fixes the 2-argument error in Fps/Cords modules
    protected void drawBackground(DrawContext drawContext, int width) {
        drawBackground(drawContext, width, 10);
    }

    protected void drawBackground(DrawContext drawContext, int width, int height) {
        drawContext.fill(x - 5, y - 5, x + width + 5, y + height + 5, backgroundColor);
    }
}