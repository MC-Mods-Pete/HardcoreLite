package net.petemc.hardcorelite.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow public abstract boolean setGameMode(GameType gameType);
    public ServerPlayerMixin(ServerLevel world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource source, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
        //HardcoreLite.LOGGER.info("PlayerHearts (UUID: " + serverPlayer.getStringUUID() + ") " + playerHearts.getNumberOfHearts());
        if (playerHearts != null) {
            playerHearts.addHeartAmount(-1);
            if (20 + playerHearts.getNumberOfHearts() * 2 == 0) {
                setGameMode(GameType.SPECTATOR);
                playerHearts.setNumberOfHearts(0);
            }
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
        }
    }
}
