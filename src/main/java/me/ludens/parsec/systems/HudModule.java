package me.ludens.parsec.systems;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule {
    public String name;
    public boolean enabled = true; // You can toggle modules on/off later
    public int x, y;

    public HudModule(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    // Every module will implement its own way of drawing
    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);
}