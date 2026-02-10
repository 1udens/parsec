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
        // Register all your modules here
        ModuleRenderer.addModule(new FpsModule());
        // ModuleRenderer.addModule(new CoordsModule()); // Adding new features is now this easy!

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            // Just tell the manager to render everything in the list
            ModuleRenderer.modules.forEach(module ->
                    module.render(drawContext, client.textRenderer)
            );
        });
        // Register modules
        ModuleRenderer.addModule(new FpsModule());
        ModuleRenderer.addModule(new CoordsModule()); // <--- Add this!

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            ModuleRenderer.modules.forEach(module ->
                    module.render(drawContext, client.textRenderer)
            );
        });
    }
}