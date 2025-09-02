package net.petemc.hardcorelite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.world.Gamerules;

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
            serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
                Level level = serverPlayer.level;
                if ((numberHearts >= 1) && (numberHearts <= level.getGameRules().getInt(Gamerules.MAXIMUM_HEARTS))) {
                    playerHearts.setNumberOfHearts(numberHearts - 10);
                    Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                    serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                    serverPlayer.sendSystemMessage(Component.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("Value needs to be between 1 and " + level.getGameRules().getInt(Gamerules.MAXIMUM_HEARTS)));
                }
            });
        }
        return 0;
    }

    private int setPlayerHearts(CommandSourceStack source, int numberHearts, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
        if (pTargets != null) {
            for (var target : pTargets) {
                if (target instanceof ServerPlayer serverPlayer) {
                    serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).ifPresent(playerHearts -> {
                        Level level = serverPlayer.level;
                        if ((numberHearts >= 1) && (numberHearts <= level.getGameRules().getInt(Gamerules.MAXIMUM_HEARTS))) {
                            playerHearts.setNumberOfHearts(numberHearts - 10);
                            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
                            serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
                            serverPlayer.sendSystemMessage(Component.literal("Changed current hearts of " + serverPlayer.getName().getString() + " to " + numberHearts));
                        } else {
                            serverPlayer.sendSystemMessage(Component.literal("Value needs to be between 1 and " + level.getGameRules().getInt(Gamerules.MAXIMUM_HEARTS)));
                        }
                    });
                }
            }
        }
        return 0;
    }
}