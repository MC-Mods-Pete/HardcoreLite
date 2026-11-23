package net.petemc.hardcorelite.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.hardcorelite.command.PlayerHeartsCommand;
import net.petemc.hardcorelite.world.Gamerules;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = HardcoreLite.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(HardcoreLite.MOD_ID, "properties"), new PlayerHeartAmountProvider());
            }
        }
    }

    // Lowest priority so other mods get the chance to cancel the death (e.g. revive mods) before a heart is taken
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Only survival and adventure deaths cost a heart: creative deaths (void, /kill while building) are not real deaths,
        // and spectators already lost all their hearts
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.isSurvival()) {
            serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
                // Only lose a heart above the minimum, so players below it (e.g. after minHearts was raised) never gain hearts by dying.
                // Only for a real minimum: with countModdedHearts the own hearts can drop to 0 and below while other mods still give hearts
                GameRules gameRules = serverPlayer.level().getGameRules();
                int minHearts = Gamerules.getMinimumHearts(gameRules);
                if (minHearts > 0 && 10 + playerHearts.getNumberOfHearts() <= minHearts) {
                    serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.min_hearts_reached", "You died, but you can't go below %s", PlayerHearts.heartsText(minHearts)).withStyle(ChatFormatting.RED));
                    return;
                }
                playerHearts.addHeartAmount(-1);
                int hearts = shownHearts(serverPlayer, playerHearts, gameRules);
                // With a minimum above 0 nobody becomes a spectator, even if other mods take away more hearts
                if (hearts <= 0 && minHearts == 0) {
                    serverPlayer.setGameMode(GameType.SPECTATOR);
                    playerHearts.setNumberOfHearts(0);
                    serverPlayer.server.getPlayerList().broadcastSystemMessage(Component.translatableWithFallback("hardcorelite.message.spectator","%s has lost their last heart and is now a spectator", serverPlayer.getDisplayName()).withStyle(ChatFormatting.RED), false);
                } else {
                    Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                    serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.lost_heart", "You lost a heart! %s left", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.RED));
                }
            });
        }
    }

    // Hearts for the messages and the spectator check. With countModdedHearts these are the hearts in the health bar:
    // own hearts plus permanent flat max health modifiers of other mods (e.g. Paraglider heart containers), which can also be negative.
    // Transient modifiers (armor, effects) and multipliers don't count, taking off armor must not cost a life.
    public static int shownHearts(ServerPlayer serverPlayer, PlayerHearts playerHearts, GameRules gameRules) {
        if (!gameRules.getBoolean(Gamerules.COUNT_MODDED_HEARTS)) {
            return 10 + playerHearts.getNumberOfHearts();
        }
        double health = 20 + playerHearts.getNumberOfHearts() * 2;
        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // Minecraft 1.20.1 has no getter for the permanent modifiers, save() writes exactly these (the ones kept in the player data)
            ListTag modifiers = maxHealth.save().getList("Modifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < modifiers.size(); i++) {
                AttributeModifier modifier = AttributeModifier.load(modifiers.getCompound(i));
                if (modifier != null && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    health += modifier.getAmount();
                }
            }
        }
        // A half heart in the bar still counts as a heart
        return (int) Math.ceil(health / 2);
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(oldPlayerHearts -> {
            event.getEntity().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(newPlayerHearts -> {
                newPlayerHearts.copyFrom(oldPlayerHearts);
                // Minecraft 1.20.1 doesn't copy the base value to the new player, so it is always set here. With countModdedHearts
                // the own hearts can be 0 or less, other mods (e.g. Paraglider) add their hearts on top again after the respawn
                int maxHealth = 20 + oldPlayerHearts.getNumberOfHearts() * 2;
                Objects.requireNonNull(event.getEntity().getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
                if (event.isWasDeath()) {
                    // Player died
                    event.getEntity().setHealth(Math.max(1, maxHealth));
                } else {
                    // Player returned from the End
                    event.getEntity().setHealth(event.getOriginal().getHealth());
                }
            });
        });
        event.getOriginal().invalidateCaps();
    }


    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        PlayerHeartsCommand.register(event.getDispatcher());
    }
}
