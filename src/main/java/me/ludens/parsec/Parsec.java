package me.ludens.parsec;

import me.ludens.parsec.config.ConfigManager;
import me.ludens.parsec.input.KeybindManager;
import me.ludens.parsec.systems.ModuleManager;
import me.ludens.parsec.systems.modules.render.CordsModule;
import me.ludens.parsec.systems.modules.render.FpsModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Parsec mod.
 * This class initializes all systems when the game starts.
 * 
 * Learning Note: ClientModInitializer is a Fabric interface that marks
 * this class as a mod entry point specifically for the client side.
 */
public class Parsec implements ClientModInitializer {
    public static final String MOD_ID = "parsec";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Singleton instance - allows other classes to access Parsec
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
        KeybindManager.register();  // Set up keybindings
        
        // Register HUD rendering
        // Learning Note: This lambda is called every frame to render HUD elements
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            // Don't render if player doesn't exist or HUD is hidden (F1 mode)
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
