package net.petemc.hardcorelite.mixin;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
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
        // Only players, other entities (e.g. foxes) also eat golden apples
        if (world.isClient || !(targetEntity instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        GameRules gameRules = world.getGameRules();
        // canRestoreHearts is the main switch for all apples
        if (!gameRules.getBoolean(ModGamerules.CAN_RESTORE_HEARTS)) {
            return;
        }
        int maxHearts;
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            maxHearts = gameRules.getInt(ModGamerules.MAXIMUM_HEARTS);
        } else if (stack.isOf(Items.GOLDEN_APPLE) && gameRules.getBoolean(ModGamerules.GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap, so they only restore lost hearts up to the default of 10
            maxHearts = Math.min(10, gameRules.getInt(ModGamerules.MAXIMUM_HEARTS));
        } else {
            return;
        }

        PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
        if (playerHearts != null && 10 + playerHearts.getNumberOfHearts() < maxHearts) {
            playerHearts.addHeartAmount(1);
            Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.sendMessage(Text.translatableWithFallback("hardcorelite.message.gained_heart",
                    "You gained a heart! %s", PlayerHearts.heartsText(10 + playerHearts.getNumberOfHearts())).formatted(Formatting.GREEN));
        }
	}
}
