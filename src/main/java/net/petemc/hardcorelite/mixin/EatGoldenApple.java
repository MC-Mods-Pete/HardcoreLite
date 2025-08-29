package net.petemc.hardcorelite.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
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

import java.util.Objects;

import static net.petemc.hardcorelite.world.Gamerules.CAN_RESTORE_HEARTS;
import static net.petemc.hardcorelite.world.Gamerules.MAXIMUM_HEARTS;

@Mixin(LivingEntity.class)
public abstract class EatGoldenApple extends Entity {
    public EatGoldenApple(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addEatEffect", at = @At("HEAD"))
    private void addEatEffect(ItemStack stack, Level level, LivingEntity targetEntity, CallbackInfo ci) {
        if (!level.isClientSide){
            if(stack.is(Items.ENCHANTED_GOLDEN_APPLE)){
                this.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
                    if (level.getGameRules().getInt(MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && level.getGameRules().getBoolean(CAN_RESTORE_HEARTS)) {
                        playerHearts.addHeartAmount(1);
                        Objects.requireNonNull(targetEntity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                    }
                });
            }
        }
    }
}
