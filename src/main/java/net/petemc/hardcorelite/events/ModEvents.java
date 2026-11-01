package net.petemc.hardcorelite.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.command.PlayerHeartsCommand;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;

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
            // Only lose a heart above the minimum, so players below it (e.g. after minHearts was raised) never gain hearts by dying
            int minHearts = ModGamerules.getMinimumHearts(serverPlayer.level().getGameRules());
            if (10 + playerHearts.getNumberOfHearts() <= minHearts) {
                serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.min_hearts_reached", "You died, but you can't go below %s", PlayerHearts.heartsText(minHearts)).withStyle(ChatFormatting.RED));
                return;
            }
            playerHearts.addHeartAmount(-1);
            int hearts = 10 + playerHearts.getNumberOfHearts();
            if (hearts <= 0) {
                serverPlayer.setGameMode(GameType.SPECTATOR);
                playerHearts.setNumberOfHearts(0);
                serverPlayer.server.getPlayerList().broadcastSystemMessage(Component.translatableWithFallback("hardcorelite.message.spectator", "%s has lost their last heart and is now a spectator", serverPlayer.getDisplayName()).withStyle(ChatFormatting.RED), false);
            } else {
                Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.lost_heart", "You lost a heart! %s left", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.RED));
            }
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
        // canRestoreHearts is the main switch for all apples
        if (!gameRules.getBoolean(ModGamerules.CAN_RESTORE_HEARTS)) {
            return;
        }

        ItemStack stack = event.getItem();
        int heartLimit;
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            heartLimit = gameRules.getInt(ModGamerules.MAXIMUM_HEARTS);
        } else if (stack.is(Items.GOLDEN_APPLE) && gameRules.getBoolean(ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap to craft, so they only give back lost hearts up to the starting 10
            heartLimit = Math.min(10, gameRules.getInt(ModGamerules.MAXIMUM_HEARTS));
        } else {
            return;
        }

        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts != null && 10 + playerHearts.getNumberOfHearts() + 1 <= heartLimit) {
            playerHearts.addHeartAmount(1);
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            int hearts = 10 + playerHearts.getNumberOfHearts();
            serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.gained_heart", "You gained a heart! %s", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.GREEN));
        }
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
}
