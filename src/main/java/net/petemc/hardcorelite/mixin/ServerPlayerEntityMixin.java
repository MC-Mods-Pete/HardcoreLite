package net.petemc.hardcorelite.mixin;

import com.mojang.authlib.GameProfile;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
	@Shadow public abstract boolean changeGameMode(GameMode gameMode);
	public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
		super(world, gameProfile);
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	private void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) (Object) this;
        PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
        //HardcoreLite.LOGGER.info("PlayerHearts (UUID: " + serverPlayer.getUuidAsString() + ") " + playerHearts.getNumberOfHearts());
        if (playerHearts != null) {
            playerHearts.addHeartAmount(-1);
            if (20 + playerHearts.getNumberOfHearts() * 2 == 0) {
                changeGameMode(GameMode.SPECTATOR);
                playerHearts.setNumberOfHearts(0);
            }
            Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
        }
	}
}
