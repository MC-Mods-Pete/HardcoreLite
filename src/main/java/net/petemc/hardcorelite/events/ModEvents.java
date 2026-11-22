package net.petemc.hardcorelite.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.command.PlayerHeartsCommand;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;
import net.petemc.hardcorelite.util.ModTags;

import java.util.Objects;

@EventBusSubscriber(modid = HardcoreLite.MOD_ID)
public class ModEvents {
    // Lowest priority so other mods get the chance to cancel the death (e.g. revive mods) before a heart is taken
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Only survival and adventure deaths cost a heart: creative deaths (void, /kill while building) are not real deaths,
        // and spectators already lost all their hearts
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.isSurvival()) {
            PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
            if (playerHearts == null) {
                return;
            }
            // Only lose a heart above the minimum, so players below it (e.g. after minHearts was raised) never gain hearts by dying.
            // Only for a real minimum: with count_modded_hearts the own hearts can drop to 0 and below while other mods still give hearts
            GameRules gameRules = serverPlayer.level().getGameRules();
            int minHearts = ModGamerules.getMinimumHearts(gameRules);
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
                Objects.requireNonNull(serverPlayer.level().getServer()).getPlayerList().broadcastSystemMessage(Component.translatableWithFallback("hardcorelite.message.spectator", "%s has lost their last heart and is now a spectator", serverPlayer.getDisplayName()).withStyle(ChatFormatting.RED), false);
            } else {
                Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.lost_heart", "You lost a heart! %s left", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.RED));
            }
        }
    }

    // Hearts for the messages and the spectator check. With count_modded_hearts these are the hearts in the health bar:
    // own hearts plus permanent flat max health modifiers of other mods (e.g. Paraglider heart containers), which can also be negative.
    // Transient modifiers (armor, effects) and multipliers don't count, taking off armor must not cost a life.
    private static int shownHearts(ServerPlayer serverPlayer, PlayerHearts playerHearts, GameRules gameRules) {
        if (!gameRules.get(ModGamerules.COUNT_MODDED_HEARTS)) {
            return 10 + playerHearts.getNumberOfHearts();
        }
        double health = 20 + playerHearts.getNumberOfHearts() * 2;
        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            for (AttributeModifier modifier : maxHealth.getPermanentModifiers()) {
                if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    health += modifier.amount();
                }
            }
        }
        // A half heart in the bar still counts as a heart
        return (int) Math.ceil(health / 2);
    }

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
                        event.getEntity().setHealth(event.getOriginal().getHealth());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        PlayerHeartsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerGamerules(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            ModGamerules.CAN_RESTORE_HEARTS = GameRules.registerBoolean("can_restore_hearts",
                    GameRuleCategory.PLAYER, true);
            ModGamerules.MAXIMUM_HEARTS = GameRules.registerInteger("max_hearts",
                    GameRuleCategory.PLAYER, 20, 0);
            ModGamerules.MINIMUM_HEARTS = GameRules.registerInteger("min_hearts",
                    GameRuleCategory.PLAYER, 0, 0);
            ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS = GameRules.registerBoolean("golden_apple_restores_hearts",
                    GameRuleCategory.PLAYER, false);
            ModGamerules.COUNT_MODDED_HEARTS = GameRules.registerBoolean("count_modded_hearts",
                    GameRuleCategory.PLAYER, false);
        }
    }

    // Fired after an item was used up, the event item is a copy from before it was eaten
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        // Only players, the event also fires on the client side
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        GameRules gameRules = serverPlayer.level().getGameRules();
        // canRestoreHearts is the main switch for all items that restore hearts
        if (!gameRules.get(ModGamerules.CAN_RESTORE_HEARTS)) {
            return;
        }

        ItemStack stack = event.getItem();
        int heartLimit;
        // Tagged items come first, so a golden apple added to the tag counts as a full item
        if (stack.is(ModTags.RESTORES_HEARTS)) {
            heartLimit = gameRules.get(ModGamerules.MAXIMUM_HEARTS);
        } else if (stack.is(Items.GOLDEN_APPLE) && gameRules.get(ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap to craft, so they only give back lost hearts up to the starting 10
            heartLimit = Math.min(10, gameRules.get(ModGamerules.MAXIMUM_HEARTS));
        } else {
            return;
        }

        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts != null && 10 + playerHearts.getNumberOfHearts() + 1 <= heartLimit) {
            playerHearts.addHeartAmount(1);
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            int hearts = shownHearts(serverPlayer, playerHearts, gameRules);
            serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.gained_heart", "You gained a heart! %s", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.GREEN));
        }
    }
}
