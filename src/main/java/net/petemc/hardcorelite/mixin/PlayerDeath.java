package net.petemc.hardcorelite.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class PlayerDeath extends Player {
    @Shadow public abstract boolean setGameMode(GameType p_143404_);

    public PlayerDeath(Level level, BlockPos blockPos, float pYRot, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
        super(level, blockPos, pYRot, gameProfile, profilePublicKey);
    }
    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource source, CallbackInfo ci) {
        this.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
            playerHearts.addHeartAmount(-1);
            if (20 + playerHearts.getNumberOfHearts() * 2 == 0) {
                setGameMode(GameType.SPECTATOR);
                playerHearts.setNumberOfHearts(0);
            } else {
                Objects.requireNonNull(((ServerPlayer) (Object) this).getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            }
        });
    }
}
