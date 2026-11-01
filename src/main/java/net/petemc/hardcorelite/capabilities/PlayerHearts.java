package net.petemc.hardcorelite.capabilities;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

    // All texts have an English fallback, for players without the mod on the client
    public static MutableText heartsText(int hearts) {
        return hearts == 1
                ? Text.translatableWithFallback("hardcorelite.hearts.one", "%s heart", hearts)
                : Text.translatableWithFallback("hardcorelite.hearts.many", "%s hearts", hearts);
    }
}
