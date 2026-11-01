package net.petemc.hardcorelite.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
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

    // Since 1.21.2 food is a Consumable, Item.finishUsingItem calls Consumable.onConsume.
    // At HEAD the stack is still the eaten item (golden apples are plain Items, only SpyglassItem overrides this method)
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        // Only players, other entities (e.g. foxes) also eat golden apples
        if (level.isClientSide() || !(livingEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        GameRules gameRules = serverPlayer.level().getGameRules();
        // canRestoreHearts is the main switch for all apples
        if (!gameRules.get(ModGamerules.CAN_RESTORE_HEARTS)) {
            return;
        }
        int maxHearts;
        if (itemStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            maxHearts = gameRules.get(ModGamerules.MAXIMUM_HEARTS);
        } else if (itemStack.is(Items.GOLDEN_APPLE) && gameRules.get(ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap, so they only restore lost hearts up to the default of 10
            maxHearts = Math.min(10, gameRules.get(ModGamerules.MAXIMUM_HEARTS));
        } else {
            return;
        }

        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts != null && 10 + playerHearts.getNumberOfHearts() < maxHearts) {
            playerHearts.addHeartAmount(1);
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.gained_heart",
                    "You gained a heart! %s", PlayerHearts.heartsText(10 + playerHearts.getNumberOfHearts())).withStyle(ChatFormatting.GREEN));
        }
    }
}
