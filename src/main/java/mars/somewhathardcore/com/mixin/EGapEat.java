package mars.somewhathardcore.com.mixin;

import mars.somewhathardcore.com.PlayerHeartAmountProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mars.somewhathardcore.com.Gamerules.CAN_RESTORE_HEARTS;
import static mars.somewhathardcore.com.Gamerules.MAXIMUM_HEARTS;

@Mixin(LivingEntity.class)
public abstract class EGapEat extends Entity {
    public EGapEat(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "addEatEffect", at = @At("HEAD"))
    private void addEatEffect(ItemStack stack, Level level, LivingEntity targetEntity, CallbackInfo ci) {
        if(!level.isClientSide){
            if(stack.is(Items.ENCHANTED_GOLDEN_APPLE)){
                this.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(thirst -> {
                    if(level.getGameRules().getInt(MAXIMUM_HEARTS) - 10 >= thirst.getHeartAmount() + 1 && level.getGameRules().getBoolean(CAN_RESTORE_HEARTS)){
                        thirst.addHeartAmount(1);
                    }
                });
            }
        }
    }
}
