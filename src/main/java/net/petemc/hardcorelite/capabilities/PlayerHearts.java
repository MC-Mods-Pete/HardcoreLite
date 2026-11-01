package net.petemc.hardcorelite.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

    // All texts have an English fallback, for players without the mod on the client
    public static MutableComponent heartsText(int hearts) {
        return hearts == 1
                ? Component.translatableWithFallback("hardcorelite.hearts.one", "%s heart", hearts)
                : Component.translatableWithFallback("hardcorelite.hearts.many", "%s hearts", hearts);
    }
}
