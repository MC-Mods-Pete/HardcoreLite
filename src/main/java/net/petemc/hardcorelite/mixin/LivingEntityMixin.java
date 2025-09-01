package net.petemc.hardcorelite.mixin;

import net.minecraft.entity.attribute.EntityAttributes;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.petemc.hardcorelite.world.ModGamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "applyFoodEffects", at = @At("HEAD"))
	private void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        if (!world.isClient){
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE){
                PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(targetEntity);
                if (playerHearts != null) {
                    if (world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && world.getGameRules().getBoolean(ModGamerules.CAN_RESTORE_HEARTS)) {
                        playerHearts.addHeartAmount(1);
                        Objects.requireNonNull(targetEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                    }
                }
            }
        }
	}
}
