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
        String text = fps + " FPS";
        int width = textRenderer.getWidth(text);

        drawBackground(drawContext, width);

        drawContext.drawText(textRenderer, text, x, y, 0xFFFFFFFF, true);
    }
}