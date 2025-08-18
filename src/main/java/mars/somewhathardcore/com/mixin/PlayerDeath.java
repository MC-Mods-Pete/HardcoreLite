package mars.somewhathardcore.com.mixin;

import com.mojang.authlib.GameProfile;
import mars.somewhathardcore.com.PlayerHeartAmountProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerDeath extends Player {
    @Shadow public abstract boolean setGameMode(GameType p_143404_);

    public PlayerDeath(Level p_250508_, BlockPos p_250289_, float p_251702_, GameProfile p_252153_) {
        super(p_250508_, p_250289_, p_251702_, p_252153_);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(thirst -> {
            if(20 + thirst.getHeartAmount() * 2== 0){
                setGameMode(GameType.SPECTATOR);
            }
            else {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20 + thirst.getHeartAmount() * 2);
            }
        });
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource source, CallbackInfo ci) {
        this.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(thirst -> {
            thirst.addHeartAmount(-1);
            System.out.println(thirst.getHeartAmount());
            //thirst.addHeartAmount(-1);
        });
    }
}
