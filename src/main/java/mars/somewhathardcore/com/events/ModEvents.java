package mars.somewhathardcore.com.events;

import mars.somewhathardcore.com.PlayerHearAmount;
import mars.somewhathardcore.com.PlayerHeartAmountProvider;
import mars.somewhathardcore.com.SomewhatHardcore;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SomewhatHardcore.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).isPresent()) {
                event.addCapability(new ResourceLocation(SomewhatHardcore.MOD_ID, "properties"), new PlayerHeartAmountProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(thirst -> {
                event.getEntity().getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(newStore -> {
                    newStore.copyFrom(thirst);
                    if(!((20 + thirst.getHeartAmount() * 2) == 0)){
                        event.getEntity().setHealth((float) (event.getEntity().getAttribute(Attributes.MAX_HEALTH).getBaseValue() + thirst.getHeartAmount() * 2));
                    }
                    //event.getEntity().sendSystemMessage(Component.literal("DEATH" + thirst.getHeartAmount()));
                    //event.getEntity().setHealth(20 + thirst.getHeartAmount() * 2);
                });
            });
            event.getOriginal().invalidateCaps();
        }
    }


    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerHearAmount.class);
    }

    /*@SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) {
            event.player.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(thirst -> {

                //event.player.sendSystemMessage(Component.literal("AMOUNT: " + thirst.getHeartAmount()));
                //System.out.println(thirst.getHeartAmount());
                    //thirst.addHeartAmount(1);
                    //System.out.println(thirst.getHeartAmount());
                    //event.player.sendSystemMessage(Component.literal("Subtracted Thirst" + thirst.getHeartAmount()));
            });
        }
    }*/
}
