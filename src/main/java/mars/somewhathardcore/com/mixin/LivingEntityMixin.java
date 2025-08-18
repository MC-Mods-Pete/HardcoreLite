package mars.somewhathardcore.com.mixin;

import mars.somewhathardcore.com.PlayerData;
import mars.somewhathardcore.com.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mars.somewhathardcore.com.YouDieYouLoseAHeart.CAN_RESTORE_HEARTS;
import static mars.somewhathardcore.com.YouDieYouLoseAHeart.MAXIMUM_HEARTS;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "applyFoodEffects", at = @At("HEAD"))
	private void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        if(!world.isClient){
            System.out.println(stack);
            if(stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE){
                PlayerData playerState = StateSaverAndLoader.getPlayerState(targetEntity);
                if(world.getGameRules().getInt(MAXIMUM_HEARTS) - 10 >= playerState.playerMaxHealth + 1 && world.getGameRules().getBoolean(CAN_RESTORE_HEARTS)){
                    playerState.playerMaxHealth = playerState.playerMaxHealth + 1;
                    PacketByteBuf data = PacketByteBufs.create();
                    data.writeInt(playerState.playerMaxHealth);
                    System.out.println(playerState.playerMaxHealth);
                }
            }
        }
	}
}
