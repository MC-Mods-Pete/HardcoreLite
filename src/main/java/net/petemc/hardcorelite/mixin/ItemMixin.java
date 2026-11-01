package net.petemc.hardcorelite.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
public abstract class ItemMixin {

    // Since 1.21.2 food is a ConsumableComponent, Item.finishUsing calls ConsumableComponent.finishConsumption.
    // At HEAD the stack is still the eaten item (golden apples are plain Items, only SpyglassItem overrides this method)
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void finishUsing(ItemStack stack, World world, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        // Only players, other entities (e.g. foxes) also eat golden apples
        if (world.isClient() || !(livingEntity instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        GameRules gameRules = serverPlayer.getEntityWorld().getGameRules();
        // canRestoreHearts is the main switch for all apples
        if (!gameRules.getValue(ModGamerules.CAN_RESTORE_HEARTS)) {
            return;
        }
        int maxHearts;
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            maxHearts = gameRules.getValue(ModGamerules.MAXIMUM_HEARTS);
        } else if (stack.isOf(Items.GOLDEN_APPLE) && gameRules.getValue(ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap, so they only restore lost hearts up to the default of 10
            maxHearts = Math.min(10, gameRules.getValue(ModGamerules.MAXIMUM_HEARTS));
        } else {
            return;
        }

        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts != null && 10 + playerHearts.getNumberOfHearts() < maxHearts) {
            playerHearts.addHeartAmount(1);
            Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.sendMessage(Text.translatableWithFallback("hardcorelite.message.gained_heart",
                    "You gained a heart! %s", PlayerHearts.heartsText(10 + playerHearts.getNumberOfHearts())).formatted(Formatting.GREEN));
        }
    }
}
