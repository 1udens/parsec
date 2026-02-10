package me.ludens.parsec.systems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class CordsModule extends HudModule {
    public CordsModule() {
        super("Coordinates", 10, 25);
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double xPos = client.player.getX();
        double yPos = client.player.getY();
        double zPos = client.player.getZ();

        String text = String.format("XYZ: %.1f, %.1f, %.1f", xPos, yPos, zPos);
        int width = textRenderer.getWidth(text);

        drawBackground(drawContext, width);
        // -------------------------

        drawContext.drawText(textRenderer, text, x, y, 0xFFAAAAAA, true);
    }
}