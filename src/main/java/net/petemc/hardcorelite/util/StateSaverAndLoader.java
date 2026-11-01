package net.petemc.hardcorelite.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
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
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) {
            return null;
        }
        StateSaverAndLoader serverState = server.getOverworld().getPersistentStateManager().getOrCreate(STATE_TYPE);
        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        PlayerHearts playerHearts = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerHearts());
        serverState.markDirty();
        return playerHearts;
    }

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(Uuids.CODEC, PlayerHearts.PLAYER_HEARTS_CODEC).fieldOf("players").forGetter(state -> state.players)
            ).apply(instance, StateSaverAndLoader::new)
    );

    // One shared instance: PersistentStateManager caches by this record (incl. the constructor reference), a new instance per call could miss the cache.
    // Must stay below CODEC, static fields are initialized in order.
    public static final PersistentStateType<StateSaverAndLoader> STATE_TYPE =
            new PersistentStateType<>(HardcoreLite.MOD_ID + "_data", StateSaverAndLoader::new, CODEC, null);
}
