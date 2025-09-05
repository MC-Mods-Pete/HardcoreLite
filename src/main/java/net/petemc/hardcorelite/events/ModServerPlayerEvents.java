package net.petemc.hardcorelite.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;

import java.util.Objects;

public class ModServerPlayerEvents {
    private static ServerPlayerEntity pOldPlayer;
    private static ServerPlayerEntity pNewPlayer;
    private static boolean pAlive;

    public ModServerPlayerEvents() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            pOldPlayer = oldPlayer;
            pNewPlayer = newPlayer;
            pAlive = alive;
            executePlayerCopied();
        });
    }

    public static void executePlayerCopied() {
        PlayerHearts oldPlayerState = HardcoreLite.serverState.getPlayerHearts(pOldPlayer);
        PlayerHearts newPlayerState = HardcoreLite.serverState.getPlayerHearts(pNewPlayer);
        if (oldPlayerState != null) {
            newPlayerState.copyFrom(oldPlayerState);
            if ((20 + oldPlayerState.getNumberOfHearts() * 2) > 0) {
                Objects.requireNonNull(pNewPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + oldPlayerState.getNumberOfHearts() * 2);
                if (!pAlive) {
                    pNewPlayer.setHealth(20 + oldPlayerState.getNumberOfHearts() * 2);
                } else {
                    pNewPlayer.setHealth(pOldPlayer.getHealth());
                }
            }
        }
    }

    public static void registerEvents() { new ModServerPlayerEvents(); }
}
