package net.petemc.hardcorelite;

import net.fabricmc.api.ModInitializer;
import net.petemc.hardcorelite.command.ModCommands;
import net.petemc.hardcorelite.events.ModServerPlayerEvents;
import net.petemc.hardcorelite.world.ModGamerules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardcoreLite implements ModInitializer {
    public static final String MOD_ID = "hardcorelite";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModGamerules.createRules();
        ModServerPlayerEvents.registerEvents();
        ModCommands.registerCommands();
    }
}