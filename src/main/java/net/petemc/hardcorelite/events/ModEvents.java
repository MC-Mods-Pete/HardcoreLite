package net.petemc.hardcorelite.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.command.SetCurrentHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;

import java.util.Objects;

public class ModEvents {
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        PlayerHearts oldPlayerHearts = StateSaverAndLoader.getPlayerHearts(event.getOriginal());
        if (oldPlayerHearts != null) {
            PlayerHearts newPlayerHearts = StateSaverAndLoader.getPlayerHearts(event.getEntity());
            if (newPlayerHearts != null) {
                newPlayerHearts.copyFrom(oldPlayerHearts);
                if ((20 + oldPlayerHearts.getNumberOfHearts() * 2) > 0) {
                    Objects.requireNonNull(event.getEntity().getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + oldPlayerHearts.getNumberOfHearts() * 2);
                    if (event.isWasDeath()) {
                        // Player died
                        event.getEntity().setHealth(20 + oldPlayerHearts.getNumberOfHearts() * 2);
                    } else {
                        // Player returned from the End
                        HardcoreLite.LOGGER.info("PlayerEvent.Clone no death");
                        event.getEntity().setHealth(event.getOriginal().getHealth());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SetCurrentHearts(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }
}
