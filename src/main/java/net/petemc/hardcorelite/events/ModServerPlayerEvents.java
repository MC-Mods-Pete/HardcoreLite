package net.petemc.hardcorelite.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;

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
        // Fired in LivingEntity.die, deaths cancelled by other mods (ALLOW_DEATH) never get here
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer serverPlayer) {
                onPlayerDeath(serverPlayer);
            }
        });
    }

    public static void onPlayerDeath(ServerPlayer serverPlayer) {
        // Only survival and adventure deaths cost a heart
        if (!serverPlayer.gameMode.isSurvival()) {
            return;
        }
        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts == null) {
            return;
        }
        // Only take a heart above the minimum, otherwise players below the minimum would gain hearts by dying
        int minHearts = ModGamerules.getMinimumHearts(serverPlayer.level().getGameRules());
        if (10 + playerHearts.getNumberOfHearts() <= minHearts) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.min_hearts_reached",
                    "You died, but you can't go below %s", PlayerHearts.heartsText(minHearts)).withStyle(ChatFormatting.RED));
            return;
        }
        playerHearts.addHeartAmount(-1);
        int hearts = 10 + playerHearts.getNumberOfHearts();
        if (hearts <= 0) {
            serverPlayer.setGameMode(GameType.SPECTATOR);
            playerHearts.setNumberOfHearts(0);
            Objects.requireNonNull(serverPlayer.level().getServer()).getPlayerList().broadcastSystemMessage(
                    Component.translatableWithFallback("hardcorelite.message.spectator",
                            "%s has lost their last heart and is now a spectator", serverPlayer.getDisplayName()).withStyle(ChatFormatting.RED), false);
        } else {
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.lost_heart",
                    "You lost a heart! %s left", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.RED));
        }
    }

    public static void executePlayerCopied() {
        PlayerHearts oldPlayerState = StateSaverAndLoader.getPlayerHearts(pOldPlayer);
        PlayerHearts newPlayerState = StateSaverAndLoader.getPlayerHearts(pNewPlayer);
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
