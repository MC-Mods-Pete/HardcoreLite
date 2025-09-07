package net.petemc.hardcorelite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    StateSaverAndLoader(){
        this (
                new HashMap<UUID, PlayerHearts>()
        );
    }

    StateSaverAndLoader(Map<UUID, PlayerHearts> players) {
        this.players = new HashMap<>(players);
    }

    public Map<UUID, PlayerHearts> players = new HashMap<>();

    public static SavedDataType<StateSaverAndLoader> createStateType() {
        return new SavedDataType<>(HardcoreLite.MOD_ID + "_data", StateSaverAndLoader::new, CODEC, null);
    }

    public PlayerHearts getPlayerHearts(LivingEntity player) {
        PlayerHearts playerHearts = null;
        if (HardcoreLite.serverState != null) {

            // Either get the player by the uuid, or we don't have data for him yet, make a new player state
            playerHearts = this.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerHearts());
        }
        this.setDirty();
        return playerHearts;
    }

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, PlayerHearts.PLAYER_HEARTS_CODEC).fieldOf("players").forGetter(state -> state.players)
            ).apply(instance, StateSaverAndLoader::new)
    );
}
