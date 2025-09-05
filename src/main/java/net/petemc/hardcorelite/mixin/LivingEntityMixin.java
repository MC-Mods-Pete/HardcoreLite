package net.petemc.hardcorelite.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.world.ModGamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addEatEffect", at = @At("HEAD"))
    private void addEatEffect(FoodProperties pFoodProperties, CallbackInfo ci) {
        Level level = this.level();
        if (!level.isClientSide) {
            if (((LivingEntity) (Object) this) instanceof ServerPlayer serverPlayer) {
                if (pFoodProperties.effects() == Foods.ENCHANTED_GOLDEN_APPLE.effects()) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                    if (playerHearts != null) {
                        if (level.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && level.getGameRules().getBoolean(ModGamerules.CAN_RESTORE_HEARTS)) {
                            playerHearts.addHeartAmount(1);
                            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                        }
                    }
                }
            }
        }
    }
}
