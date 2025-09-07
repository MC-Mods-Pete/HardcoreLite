package net.petemc.hardcorelite.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;

public class PlayerHearts {
    public static final Codec<PlayerHearts> PLAYER_HEARTS_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("playerMaxHealth").forGetter(PlayerHearts::getNumberOfHearts)
            ).apply(instance, PlayerHearts::new)
    );

    public PlayerHearts() {
        numberOfHearts = 0;
    }

    public PlayerHearts(Integer value) {
        numberOfHearts = value;
    }

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
        numberOfHearts = nbt.getInt("numberOfHearts").orElse(0);
    }
}
