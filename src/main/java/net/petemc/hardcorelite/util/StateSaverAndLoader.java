package net.petemc.hardcorelite.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    public HashMap<UUID, PlayerHearts> players = new HashMap<>();

    public PlayerHearts getPlayerHearts(LivingEntity player) {
        PlayerHearts playerHearts = null;
        if (player.level().getServer() != null) {

            // Either get the player by the uuid, or we don't have data for him yet, make a new player state
            playerHearts = this.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerHearts());
        }
        this.setDirty();
        return playerHearts;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        CompoundTag playersNbt = new CompoundTag();
        players.forEach((uuid, playerHearts) -> {
            CompoundTag playerNbt = new CompoundTag();

            playerNbt.putInt("playerMaxHealth", playerHearts.getNumberOfHearts());

            playersNbt.put(uuid.toString(), playerNbt);
        });
        tag.put("players", playersNbt);

        return tag;
    }

    public static StateSaverAndLoader load(CompoundTag tag, HolderLookup.Provider registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        CompoundTag playersNbt = tag.getCompound("players");
        playersNbt.getAllKeys().forEach(key -> {
            PlayerHearts playerHearts = new PlayerHearts();

            playerHearts.setNumberOfHearts(playersNbt.getCompound(key).getInt("playerMaxHealth"));

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerHearts);
        });
        state.setDirty();

        return state;
    }

    public static Factory<StateSaverAndLoader> factory() {
        return new Factory<>(StateSaverAndLoader::new, StateSaverAndLoader::load, null);
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), HardcoreLite.MOD_ID);
    }
}
