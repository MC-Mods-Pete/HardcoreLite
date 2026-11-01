package net.petemc.hardcorelite.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

    public void saveNBTData(NbtCompound nbt){
        nbt.putInt("numberOfHearts", numberOfHearts);
    }

    public void loadNBTData(NbtCompound nbt){
        numberOfHearts = nbt.getInt("numberOfHearts").orElse(0);
    }

    // All texts have an English fallback, for players without the mod on the client
    public static MutableText heartsText(int hearts) {
        return hearts == 1
                ? Text.translatableWithFallback("hardcorelite.hearts.one", "%s heart", hearts)
                : Text.translatableWithFallback("hardcorelite.hearts.many", "%s hearts", hearts);
    }
}
