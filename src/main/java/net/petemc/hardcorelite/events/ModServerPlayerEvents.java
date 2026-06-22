package net.petemc.hardcorelite.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;

import java.util.Objects;

public class ModServerPlayerEvents {
    private static ServerPlayer pOldPlayer;
    private static ServerPlayer pNewPlayer;
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
                Objects.requireNonNull(pNewPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + oldPlayerState.getNumberOfHearts() * 2);
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
