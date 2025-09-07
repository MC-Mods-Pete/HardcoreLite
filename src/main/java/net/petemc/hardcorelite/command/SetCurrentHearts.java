package net.petemc.hardcorelite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.hardcorelite.HardcoreLite;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.world.ModGamerules;

import java.util.Collection;
import java.util.Objects;

public class SetCurrentHearts {
    public static Collection<? extends Entity> entities = null;

    public SetCurrentHearts(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerhearts")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("numberHearts", IntegerArgumentType.integer(1))
                .executes((command) -> {
                    return setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"));
                })));
        dispatcher.register(Commands.literal("playerhearts")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("numberHearts", IntegerArgumentType.integer(1))
                .then(Commands.argument("targets", EntityArgument.entities()).executes((command) -> {
                    return setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"), EntityArgument.getEntities(command, "targets"));
                }))));
    }

    private int setPlayerHearts(CommandSourceStack source, int numberHearts) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
            if (playerHearts != null) {
                ServerLevel serverLevel = serverPlayer.level();
                if ((numberHearts >= 1) && (numberHearts <= serverLevel.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS))) {
                    playerHearts.setNumberOfHearts(numberHearts - 10);
                    Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                    serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                    serverPlayer.sendSystemMessage(Component.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("Value needs to be between 1 and " + serverLevel.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS)));
                }
            }
        }
        return 0;
    }

    private int setPlayerHearts(CommandSourceStack source, int numberHearts, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
        if (pTargets != null) {
            for (var target : pTargets) {
                if (target instanceof ServerPlayer serverPlayer) {
                    PlayerHearts playerHearts = HardcoreLite.serverState.getPlayerHearts(serverPlayer);
                    if (playerHearts != null) {
                        ServerLevel serverLevel = serverPlayer.level();
                        if ((numberHearts >= 1) && (numberHearts <= serverLevel.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS))) {
                            playerHearts.setNumberOfHearts(numberHearts - 10);
                            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                            serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                            serverPlayer.sendSystemMessage(Component.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
                        } else {
                            serverPlayer.sendSystemMessage(Component.literal("Value needs to be between 1 and " + serverLevel.getGameRules().getInt(ModGamerules.MAXIMUM_HEARTS)));
                        }
                    }
                }
            }
        }
        return 0;
    }
}