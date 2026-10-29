package net.petemc.hardcorelite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.petemc.hardcorelite.capabilities.PlayerHeartAmountProvider;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.world.Gamerules;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerHeartsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerhearts")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get")
                        .executes(context -> getPlayerHearts(context.getSource(), List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> getPlayerHearts(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("set")
                        .then(setArguments()))
                // Short form without "set" from 1.0.0, kept so existing command blocks keep working
                .then(setArguments()));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> setArguments() {
        return Commands.argument("numberHearts", IntegerArgumentType.integer(1))
                .executes(context -> setPlayerHearts(context.getSource(), IntegerArgumentType.getInteger(context, "numberHearts"), List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> setPlayerHearts(context.getSource(), IntegerArgumentType.getInteger(context, "numberHearts"), EntityArgument.getPlayers(context, "targets"))));
    }

    // Returns the number of players whose hearts were changed (0 = failure)
    private static int setPlayerHearts(CommandSourceStack source, int numberHearts, Collection<ServerPlayer> targets) {
        int maxHearts = source.getServer().getGameRules().getInt(Gamerules.MAXIMUM_HEARTS);
        int minHearts = Math.max(1, Gamerules.getMinimumHearts(source.getServer().getGameRules()));
        if (numberHearts < minHearts || numberHearts > maxHearts) {
            source.sendFailure(Component.literal("Value needs to be between " + minHearts + " and " + maxHearts));
            return 0;
        }

        int changed = 0;
        Component lastChangedName = Component.empty();
        for (ServerPlayer serverPlayer : targets) {
            PlayerHearts playerHearts = serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).orElse(null);
            if (playerHearts == null) {
                continue;
            }
            playerHearts.setNumberOfHearts(numberHearts - 10);
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
            // Like vanilla /gamemode: tell the target if someone else changed their hearts
            if (source.getEntity() != serverPlayer && source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                serverPlayer.sendSystemMessage(Component.literal("Your hearts have been set to " + numberHearts));
            }
            lastChangedName = serverPlayer.getDisplayName();
            changed++;
        }

        if (changed == 1) {
            source.sendSuccess(Component.literal("Changed current hearts of ").append(lastChangedName).append(" to " + numberHearts), true);
        } else if (changed > 1) {
            source.sendSuccess(Component.literal("Changed current hearts of " + changed + " players to " + numberHearts), true);
        }
        return changed;
    }

    // Returns the hearts of the player for a single target (usable with /execute store), otherwise the number of players queried
    private static int getPlayerHearts(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int hearts = 0;
        int queried = 0;
        for (ServerPlayer serverPlayer : targets) {
            PlayerHearts playerHearts = serverPlayer.getCapability(PlayerHeartAmountProvider.PLAYER_HEART_AMOUNT).orElse(null);
            if (playerHearts == null) {
                continue;
            }
            hearts = 10 + playerHearts.getNumberOfHearts();
            source.sendSuccess(Component.empty().append(serverPlayer.getDisplayName()).append(" has ").append(PlayerHearts.heartsText(hearts)), false);
            queried++;
        }
        return targets.size() == 1 ? hearts : queried;
    }
}
