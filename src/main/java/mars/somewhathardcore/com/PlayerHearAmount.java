package mars.somewhathardcore.com;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

//@AutoRegisterCapability
public class PlayerHearAmount {
    private int heartAmount;

    public int getHeartAmount() {
        return heartAmount;
    }

    public void addHeartAmount(int heartAmount) {
        this.heartAmount += heartAmount;
    }

    public void copyFrom(PlayerHearAmount source){
        this.heartAmount = source.heartAmount;
    }

    public void saveNBTData(CompoundTag nbt){
        nbt.putInt("hearAmount", heartAmount);
    }

    public void loadNBTData(CompoundTag nbt){
        heartAmount = nbt.getInt("hearAmount");
    }
}
