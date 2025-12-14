package net.petemc.hardcorelite.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.command.SetCurrentHearts;
import net.petemc.hardcorelite.world.ModGamerules;

import java.util.Objects;

@EventBusSubscriber(modid = HardcoreLite.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        PlayerHearts oldPlayerHearts = HardcoreLite.serverState.getPlayerHearts(event.getOriginal());
        if (oldPlayerHearts != null) {
            PlayerHearts newPlayerHearts = HardcoreLite.serverState.getPlayerHearts(event.getEntity());
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
        new SetCurrentHearts(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerGamerules(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            ModGamerules.CAN_RESTORE_HEARTS = GameRules.registerBoolean("can_restore_hearts",
                    GameRuleCategory.PLAYER, true);
            ModGamerules.MAXIMUM_HEARTS = GameRules.registerInteger("max_hearts",
                    GameRuleCategory.PLAYER, 20, 0);
        }
    }

    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                if (!serverLevel.isClientSide()) {
                    if (event.getItem().getItem().toString().contains("enchanted_golden_apple")) {
                        PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                        if (playerHearts != null) {
                            if (serverLevel.getGameRules().get(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && serverLevel.getGameRules().get(ModGamerules.CAN_RESTORE_HEARTS)) {
                                playerHearts.addHeartAmount(1);
                                Objects.requireNonNull((serverPlayer).getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                            }
                        }
                    }
                }
            }
        }
    }
}

