package net.petemc.hardcorelite.capabilities;

import net.minecraft.nbt.CompoundTag;

public class PlayerHearts {
    private int numberOfHearts;

    public void setNumberOfHearts(int val) { numberOfHearts = val; }

    public int getNumberOfHearts() {
        return numberOfHearts;
    }

    public void addHeartAmount(int value) {
        this.numberOfHearts += value;
    }

    public void copyFrom(PlayerHearts source){
        this.numberOfHearts = source.numberOfHearts;
    }

    public void saveNBTData(CompoundTag nbt){
        nbt.putInt("numberOfHearts", numberOfHearts);
    }

    public void loadNBTData(CompoundTag nbt){
        numberOfHearts = nbt.getInt("numberOfHearts");
    }
}
