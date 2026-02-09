package me.ludens.parsec; // Replace 'yourname' with what you used in setup

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class parsec implements ClientModInitializer {
    public static final String MOD_ID = "parsec";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // This code runs only on the client side during startup
        LOGGER.info("Parsec utility initialized. Ready for flight.");
    }
}