package net.petemc.hardcorelite.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void finishUsingMixin(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            if (!level.isClientSide()) {
                if (itemStack.getItem().toString().contains("enchanted_golden_apple")) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                    if (playerHearts != null) {
                        if (level instanceof ServerLevel serverLevel) {
                            if (serverLevel.getGameRules().get(ModGamerules.MAXIMUM_HEARTS) - 10 >= playerHearts.getNumberOfHearts() + 1 && serverLevel.getGameRules().get(ModGamerules.CAN_RESTORE_HEARTS)) {
                                playerHearts.addHeartAmount(1);
                                Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
