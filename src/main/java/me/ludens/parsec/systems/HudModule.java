package me.ludens.parsec.systems;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule {
    public String name;
    public boolean enabled = true;
    public int x, y;

    public HudModule(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext drawContext, TextRenderer textRenderer);
}