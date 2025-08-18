package mars.somewhathardcore.com;

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
    public static Capability<PlayerHearAmount> PLAYER_HEART_AMOUNT = CapabilityManager.get(new CapabilityToken<PlayerHearAmount>() {});

    private PlayerHearAmount hearAmount = null;
    private final LazyOptional<PlayerHearAmount> optional = LazyOptional.of(this::createHeartAmount);

    private PlayerHearAmount createHeartAmount(){
        if(this.hearAmount == null){
            this.hearAmount = new PlayerHearAmount();
        }

        return this.hearAmount;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_HEART_AMOUNT){
            return optional.cast();
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
