package me.ludens.parsec.systems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class CoordsModule extends HudModule {
    public CoordsModule() {
        // We set y to 25 so it sits right below the FPS box (which is at 10)
        super("Coordinates", 10, 25);
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Get coordinates and format to 1 decimal place
        double xPos = client.player.getX();
        double yPos = client.player.getY();
        double zPos = client.player.getZ();

        String text = String.format("XYZ: %.1f, %.1f, %.1f", xPos, yPos, zPos);

        int width = textRenderer.getWidth(text);

        // Draw background
        drawContext.fill(x - 5, y - 5, x + width + 5, y + 12, 0xAA000000);

        // Draw text in a nice light-purple/aqua color (0xFFFFFFAA for light blue)
        drawContext.drawText(textRenderer, text, x, y, 0xFFAAAAAA, true);
    }
}