package net.petemc.hardcorelite.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
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
import static net.petemc.hardcorelite.world.Gamerules.GOLDEN_APPLE_RESTORES_HEARTS;
import static net.petemc.hardcorelite.world.Gamerules.MAXIMUM_HEARTS;

@Mixin(LivingEntity.class)
public abstract class EatGoldenApple extends Entity {
    public EatGoldenApple(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addEatEffect", at = @At("HEAD"))
    private void addEatEffect(ItemStack stack, Level level, LivingEntity targetEntity, CallbackInfo ci) {
        // Only players, other entities (e.g. foxes) also eat golden apples
        if (level.isClientSide || !(targetEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // canRestoreHearts is the main switch for all apples
        if (!level.getGameRules().getBoolean(CAN_RESTORE_HEARTS)) {
            return;
        }

        int heartLimit;
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            heartLimit = level.getGameRules().getInt(MAXIMUM_HEARTS);
        } else if (stack.is(Items.GOLDEN_APPLE) && level.getGameRules().getBoolean(GOLDEN_APPLE_RESTORES_HEARTS)) {
            // Normal golden apples are cheap to craft, so they only give back lost hearts up to the starting 10
            heartLimit = Math.min(10, level.getGameRules().getInt(MAXIMUM_HEARTS));
        } else {
            return;
        }

        serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
            if (10 + playerHearts.getNumberOfHearts() + 1 <= heartLimit) {
                playerHearts.addHeartAmount(1);
                Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                int hearts = 10 + playerHearts.getNumberOfHearts();
                serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.message.gained_heart", "You gained a heart! %s", PlayerHearts.heartsText(hearts)).withStyle(ChatFormatting.GREEN));
            }
        });
    }
}
