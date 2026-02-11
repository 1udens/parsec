package me.ludens.parsec.systems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class CordsModule extends HudModule {
    private String cachedText = "";
    private int lastX = Integer.MAX_VALUE;
    private int lastY = Integer.MAX_VALUE;
    private int lastZ = Integer.MAX_VALUE;

    public CordsModule() {
        super("Coordinates", 10, 25);
    }

    @Override
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int xPos = (int) client.player.getX();
        int yPos = (int) client.player.getY();
        int zPos = (int) client.player.getZ();

        // Only update text if position changed
        if (xPos != lastX || yPos != lastY || zPos != lastZ) {
            cachedText = String.format("XYZ: %d, %d, %d", xPos, yPos, zPos);
            lastX = xPos;
            lastY = yPos;
            lastZ = zPos;
        }

        int width = textRenderer.getWidth(cachedText);
        drawBackground(drawContext, width);
        drawContext.drawText(textRenderer, cachedText, x, y, 0xFFAAAAAA, true);
    }
}