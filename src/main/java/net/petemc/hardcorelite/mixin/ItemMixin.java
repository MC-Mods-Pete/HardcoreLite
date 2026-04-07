package net.petemc.hardcorelite.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.world.ModGamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void finishUsingMixin(ItemStack stack, World world, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayerEntity serverPlayer) {
            if (!world.isClient()) {
                if (stack.getItem().toString().contains("enchanted_golden_apple")) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                    if (playerHearts != null) {
                        if (world instanceof ServerWorld serverLevel) {
                            if (serverLevel.getGameRules().getValue(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && serverLevel.getGameRules().getValue(ModGamerules.CAN_RESTORE_HEARTS)) {
                                playerHearts.addHeartAmount(1);
                                Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
