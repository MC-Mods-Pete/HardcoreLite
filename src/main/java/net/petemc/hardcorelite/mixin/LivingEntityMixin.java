package net.petemc.hardcorelite.mixin;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
	private void applyFoodEffects(FoodComponent component, CallbackInfo ci) {
        World world = this.getWorld();
        if (!world.isClient) {
            if (((LivingEntity) (Object) this) instanceof PlayerEntity playerEntity) {
                if (component.effects() == FoodComponents.ENCHANTED_GOLDEN_APPLE.effects()) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(playerEntity);
                    if (playerHearts != null) {
                        if (world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && world.getGameRules().getBoolean(ModGamerules.CAN_RESTORE_HEARTS)) {
                            playerHearts.addHeartAmount(1);
                            Objects.requireNonNull(playerEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                        }
                    }
                }
            }
        }
	}
}
