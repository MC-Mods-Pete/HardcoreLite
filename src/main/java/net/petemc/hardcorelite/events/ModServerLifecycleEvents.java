package net.petemc.hardcorelite.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.util.StateSaverAndLoader;

public class ModServerLifecycleEvents {

    private static MinecraftServer pServer;

    public ModServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            pServer = server;
            executeServerStarted();
        });
    }

    public static void executeServerStarted() {
        if (HardcoreLite.serverState == null) {
            HardcoreLite.serverState = StateSaverAndLoader.getServerState(pServer);
        }
    }

    public static void registerEvents() { new ModServerLifecycleEvents(); }
}
