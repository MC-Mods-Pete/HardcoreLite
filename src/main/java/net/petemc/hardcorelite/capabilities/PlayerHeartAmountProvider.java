package net.petemc.hardcorelite.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerHeartAmountProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerHearts> PLAYER_HEART_AMOUNT = CapabilityManager.get(new CapabilityToken<PlayerHearts>() {});

    private PlayerHearts playerHearts = null;
    private final LazyOptional<PlayerHearts> playerHeartsLazyOptional = LazyOptional.of(this::createHeartAmount);

    private PlayerHearts createHeartAmount() {
        if (this.playerHearts == null) {
            this.playerHearts = new PlayerHearts();
        }

        return this.playerHearts;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == PLAYER_HEART_AMOUNT){
            return playerHeartsLazyOptional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHeartAmount().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHeartAmount().loadNBTData(nbt);
    }
}
