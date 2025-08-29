package net.petemc.hardcorelite.events;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.server.command.ConfigCommand;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.HardcoreLite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.hardcorelite.command.SetCurrentHearts;

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
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerHearts.class);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SetCurrentHearts(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
