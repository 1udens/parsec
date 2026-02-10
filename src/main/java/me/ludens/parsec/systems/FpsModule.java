package me.ludens.parsec.systems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class FpsModule extends HudModule {
    public FpsModule() {
        super("FPS", 10, 10);
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        if (!enabled) return;

        int fps = MinecraftClient.getInstance().getCurrentFps();
        String text = "FPS: " + fps;

        int width = textRenderer.getWidth(text);
        drawContext.fill(x - 5, y - 5, x + width + 5, y + 12, 0xAA000000);
        drawContext.drawText(textRenderer, text, x, y, 0xFFFFFFFF, true);
    }
}