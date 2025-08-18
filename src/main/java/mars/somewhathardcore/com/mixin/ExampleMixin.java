package mars.somewhathardcore.com.mixin;

import com.mojang.authlib.GameProfile;
import mars.somewhathardcore.com.PlayerData;
import mars.somewhathardcore.com.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ExampleMixin extends PlayerEntity {
	@Shadow public abstract boolean changeGameMode(GameMode gameMode);
	public ExampleMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "onDeath", at = @At("TAIL"))
	private void onDeath(DamageSource source, CallbackInfo ci) {
		System.out.println("onDeath");
		addToHealthAmount(-1);
	}

	@Unique
	public void addToHealthAmount(int heartAmount){
		PlayerData playerState = StateSaverAndLoader.getPlayerState(this);
		playerState.playerMaxHealth = playerState.playerMaxHealth + heartAmount;
		PacketByteBuf data = PacketByteBufs.create();
		data.writeInt(playerState.playerMaxHealth);
		System.out.println(playerState.playerMaxHealth);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void tick(CallbackInfo ci) {
		PlayerData playerState = StateSaverAndLoader.getPlayerState(this);
		int maxHealth = playerState.playerMaxHealth;
		if(20 + maxHealth * 2== 0){
			changeGameMode(GameMode.SPECTATOR);
		}

		this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20 + maxHealth * 2);
	}
}