package net.petemc.hardcorelite.capabilities;

import net.minecraft.nbt.NbtCompound;

public class PlayerHearts {
    private int numberOfHearts = 0;

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

    public void saveNBTData(NbtCompound nbt){
        nbt.putInt("numberOfHearts", numberOfHearts);
    }

    public void loadNBTData(NbtCompound nbt){
        numberOfHearts = nbt.getInt("numberOfHearts");
    }
}
