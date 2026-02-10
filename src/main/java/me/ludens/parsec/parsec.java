package me.ludens.parsec;

import me.ludens.parsec.systems.CoordsModule;
import me.ludens.parsec.systems.FpsModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class parsec implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModuleRenderer.addModule(new FpsModule());
        ModuleRenderer.addModule(new CoordsModule());

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            ModuleRenderer.modules.forEach(module ->
                    module.render(drawContext, client.textRenderer)
            );
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            ModuleRenderer.modules.forEach(module ->
                    module.render(drawContext, client.textRenderer)
            );
        });
    }
}