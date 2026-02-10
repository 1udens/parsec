package me.ludens.parsec;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class parsec implements ClientModInitializer {
    public static final String MOD_ID = "parsec";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Parsec utility initialized. Ready for flight...");

        // Register the client-side command /parsec
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("parsec")
                    .executes(context -> {
                        // This message is only visible to you, not other players on a server
                        context.getSource().sendFeedback(Text.literal("§bparsec§r: Utility system online."));
                        return 1;
                    }));
        });
    }
}