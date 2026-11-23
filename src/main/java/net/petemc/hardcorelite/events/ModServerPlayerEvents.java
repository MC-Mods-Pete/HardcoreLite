package net.petemc.hardcorelite.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;

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
        // Fired at the end of ServerPlayerEntity.onDeath, so deaths cancelled by other mods (ALLOW_DEATH) never get here
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                onPlayerDeath(serverPlayer);
            }
        });
    }

    public static void onPlayerDeath(ServerPlayerEntity serverPlayer) {
        // Only survival and adventure deaths cost a heart
        if (!serverPlayer.interactionManager.isSurvivalLike()) {
            return;
        }
        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts == null) {
            return;
        }
        // Only take a heart above the minimum, otherwise players below the minimum would gain hearts by dying.
        // Only for a real minimum: with countModdedHearts the own hearts can drop to 0 and below while other mods still give hearts
        GameRules gameRules = serverPlayer.getWorld().getGameRules();
        int minHearts = ModGamerules.getMinimumHearts(gameRules);
        if (minHearts > 0 && 10 + playerHearts.getNumberOfHearts() <= minHearts) {
            serverPlayer.sendMessage(Text.translatableWithFallback("hardcorelite.message.min_hearts_reached",
                    "You died, but you can't go below %s", PlayerHearts.heartsText(minHearts)).formatted(Formatting.RED));
            return;
        }
        playerHearts.addHeartAmount(-1);
        int hearts = shownHearts(serverPlayer, playerHearts, gameRules);
        // With a minimum above 0 nobody becomes a spectator, even if other mods take away more hearts
        if (hearts <= 0 && minHearts == 0) {
            serverPlayer.changeGameMode(GameMode.SPECTATOR);
            playerHearts.setNumberOfHearts(0);
            Objects.requireNonNull(serverPlayer.getServer()).getPlayerManager().broadcast(
                    Text.translatableWithFallback("hardcorelite.message.spectator",
                            "%s has lost their last heart and is now a spectator", serverPlayer.getDisplayName()).formatted(Formatting.RED), false);
        } else {
            Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.sendMessage(Text.translatableWithFallback("hardcorelite.message.lost_heart",
                    "You lost a heart! %s left", PlayerHearts.heartsText(hearts)).formatted(Formatting.RED));
        }
    }

    // Hearts for the messages and the spectator check. With countModdedHearts these are the hearts in the health bar:
    // own hearts plus permanent flat max health modifiers of other mods (e.g. Paraglider heart containers), which can also be negative.
    // Temporary modifiers (armor, effects) and multipliers don't count, taking off armor must not cost a life.
    public static int shownHearts(ServerPlayerEntity serverPlayer, PlayerHearts playerHearts, GameRules gameRules) {
        if (!gameRules.getBoolean(ModGamerules.COUNT_MODDED_HEARTS)) {
            return 10 + playerHearts.getNumberOfHearts();
        }
        double health = 20 + playerHearts.getNumberOfHearts() * 2;
        EntityAttributeInstance maxHealth = serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            // Minecraft 1.21 has no getter for the persistent modifiers, toNbt() writes exactly these (the ones kept in the player data)
            NbtList modifiers = maxHealth.toNbt().getList("modifiers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < modifiers.size(); i++) {
                EntityAttributeModifier modifier = EntityAttributeModifier.fromNbt(modifiers.getCompound(i));
                if (modifier != null && modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                    health += modifier.value();
                }
            }
        }
        // A half heart in the bar still counts as a heart
        return (int) Math.ceil(health / 2);
    }

    public static void executePlayerCopied() {
        PlayerHearts oldPlayerState = StateSaverAndLoader.getPlayerHearts(pOldPlayer);
        PlayerHearts newPlayerState = StateSaverAndLoader.getPlayerHearts(pNewPlayer);
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
