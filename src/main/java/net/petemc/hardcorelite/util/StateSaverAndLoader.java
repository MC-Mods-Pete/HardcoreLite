package net.petemc.hardcorelite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
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

    // Always looked up through the current server, so every world uses its own data
    public static PlayerHearts getPlayerHearts(LivingEntity player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return null;
        }
        StateSaverAndLoader serverState = server.overworld().getDataStorage().computeIfAbsent(STATE_TYPE);
        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        PlayerHearts playerHearts = serverState.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerHearts());
        serverState.setDirty();
        return playerHearts;
    }

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, PlayerHearts.PLAYER_HEARTS_CODEC).fieldOf("players").forGetter(state -> state.players)
            ).apply(instance, StateSaverAndLoader::new)
    );

    // One shared instance: SavedDataStorage caches by this record (incl. the constructor reference), a new instance per call could miss the cache.
    // Must stay below CODEC, static fields are initialized in order.
    public static final SavedDataType<StateSaverAndLoader> STATE_TYPE =
            new SavedDataType<>(Identifier.parse(HardcoreLite.MOD_ID + "_data"), StateSaverAndLoader::new, CODEC, null);
}
