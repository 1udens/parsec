package me.ludens.parsec;

import me.ludens.parsec.config.ConfigManager;
import me.ludens.parsec.input.InputHandler;
import me.ludens.parsec.systems.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Parsec mod.
 * UPDATED to use the new InputHandler system (similar to Meteor Client)
 */
public class Parsec implements ClientModInitializer {
    public static final String MOD_ID = "parsec";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static Parsec instance;
    
    public static Parsec getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Parsec utility initializing...");

        // Initialize systems in order
        ModuleManager.init();      // Register all modules
        ConfigManager.load();       // Load saved configuration
        InputHandler.init();        // NEW: Initialize input handler (replaces KeybindManager)
        
        // Register HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            // Render all enabled modules
            ModuleManager.INSTANCE.getModules().forEach(module -> {
                if (module.isEnabled()) {
                    module.render(drawContext, client.textRenderer);
                }
            });
        });
        
        LOGGER.info("Parsec utility initialized successfully!");
    }
}
