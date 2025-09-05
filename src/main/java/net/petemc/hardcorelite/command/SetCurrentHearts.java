package net.petemc.hardcorelite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;

import java.awt.*;
import java.util.Collection;
import java.util.Objects;

public class SetCurrentHearts {
    public static Collection<? extends Entity> entities = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("playerhearts")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("numberHearts", IntegerArgumentType.integer(1))
                .executes((command) -> {
                    return setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"));
                })));
        dispatcher.register(CommandManager.literal("playerhearts")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("numberHearts", IntegerArgumentType.integer(1))
                .then(CommandManager.argument("targets", EntityArgumentType.entities()).executes((command) -> {
                    return setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"), EntityArgumentType.getEntities(command, "targets"));
                }))));
    }

    private static int setPlayerHearts(ServerCommandSource source, int numberHearts) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayerEntity serverPlayer) {
            PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
            World world = serverPlayer.getWorld();
            if ((numberHearts >= 1) && (numberHearts <= world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS))) {
                playerHearts.setNumberOfHearts(numberHearts - 10);
                Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                serverPlayer.sendMessage(Text.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
            } else {
                serverPlayer.sendMessage(Text.literal("Value needs to be between 1 and " + world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS)));
            }
        }
        return 0;
    }

    private static int setPlayerHearts(ServerCommandSource source, int numberHearts, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
        if (pTargets != null) {
            for (var target : pTargets) {
                if (target instanceof ServerPlayerEntity serverPlayer) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                    World world = serverPlayer.getWorld();
                    if ((numberHearts >= 1) && (numberHearts <= world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS))) {
                        playerHearts.setNumberOfHearts(numberHearts - 10);
                        Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                        serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                        serverPlayer.sendMessage(Text.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
                    } else {
                        serverPlayer.sendMessage(Text.literal("Value needs to be between 1 and " + world.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS)));
                    }
                }
            }
        }
        return 0;
    }
}
