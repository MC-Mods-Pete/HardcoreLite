package net.petemc.hardcorelite.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
                // Only lose a heart above the minimum, so players below it (e.g. after minHearts was raised) never gain hearts by dying
                int minHearts = Gamerules.getMinimumHearts(serverPlayer.level().getGameRules());
                if (10 + playerHearts.getNumberOfHearts() <= minHearts) {
                    serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.min_hearts_reached", "You died, but you can't go below %s", PlayerHearts.heartsText(minHearts)).withStyle(ChatFormatting.RED));
                    return;
                }
                playerHearts.addHeartAmount(-1);
                int hearts = 10 + playerHearts.getNumberOfHearts();
                if (hearts <= 0) {
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

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(oldPlayerHearts -> {
            event.getEntity().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(newPlayerHearts -> {
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
            });
        });
        event.getOriginal().invalidateCaps();
    }


    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        PlayerHeartsCommand.register(event.getDispatcher());
    }
}
