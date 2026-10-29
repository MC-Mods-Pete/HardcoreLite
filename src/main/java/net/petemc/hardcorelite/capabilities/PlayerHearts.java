package net.petemc.hardcorelite.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
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

    // Minecraft 1.19.2 has no translatableWithFallback, so all chat texts are English
    // (same wording as en_us.json in the versions with translatable texts)
    public static MutableComponent heartsText(int hearts) {
        return Component.literal(hearts == 1 ? hearts + " heart" : hearts + " hearts");
    }
}
