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
import net.minecraft.world.level.gamerules.GameRules;
import net.petemc.hardcorelite.capabilities.PlayerHearts;
import net.petemc.hardcorelite.util.StateSaverAndLoader;
import net.petemc.hardcorelite.world.ModGamerules;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerHeartsCommand {

    /*
     * /playerhearts get [<targets>]
     * /playerhearts set <numberHearts> [<targets>]
     * /playerhearts <numberHearts> [<targets>]      (short form from 1.0.0)
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerhearts")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("get")
                        .executes(context -> getPlayerHearts(context.getSource(), List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> getPlayerHearts(context.getSource(), EntityArgument.getPlayers(context, "targets")))))
                .then(Commands.literal("set")
                        .then(setArguments()))
                // Short form without "set" from 1.0.0, kept so existing command blocks keep working
                .then(setArguments()));
    }

    // Shared by "set" and the short form
    private static RequiredArgumentBuilder<CommandSourceStack, Integer> setArguments() {
        return Commands.argument("numberHearts", IntegerArgumentType.integer(1))
                .executes(context -> setPlayerHearts(context.getSource(), IntegerArgumentType.getInteger(context, "numberHearts"), List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> setPlayerHearts(context.getSource(), IntegerArgumentType.getInteger(context, "numberHearts"), EntityArgument.getPlayers(context, "targets"))));
    }

    // Returns the number of players whose hearts were changed (0 = failure)
    private static int setPlayerHearts(CommandSourceStack source, int numberHearts, Collection<ServerPlayer> targets) {
        GameRules gameRules = source.getServer().getGameRules();
        int maxHearts = gameRules.get(ModGamerules.MAXIMUM_HEARTS);
        int minHearts = Math.max(1, ModGamerules.getMinimumHearts(gameRules));
        if (numberHearts < minHearts || numberHearts > maxHearts) {
            source.sendFailure(Component.translatableWithFallback("hardcorelite.command.set.invalid_value", "Value needs to be between %s and %s", minHearts, maxHearts));
            return 0;
        }

        int changed = 0;
        Component lastChangedName = Component.empty();
        for (ServerPlayer serverPlayer : targets) {
            PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
            if (playerHearts == null) {
                continue;
            }
            playerHearts.setNumberOfHearts(numberHearts - 10);
            Objects.requireNonNull(serverPlayer.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
            // Like vanilla /gamemode: tell the target if someone else changed their hearts
            if (source.getEntity() != serverPlayer && source.getLevel().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                serverPlayer.sendSystemMessage(Component.translatableWithFallback("hardcorelite.command.set.target", "Your hearts have been set to %s", numberHearts));
            }
            lastChangedName = serverPlayer.getDisplayName();
            changed++;
        }

        if (changed == 1) {
            Component name = lastChangedName;
            source.sendSuccess(() -> Component.translatableWithFallback("hardcorelite.command.set.single", "Changed current hearts of %s to %s", name, numberHearts), true);
        } else if (changed > 1) {
            int count = changed;
            source.sendSuccess(() -> Component.translatableWithFallback("hardcorelite.command.set.multiple", "Changed current hearts of %s players to %s", count, numberHearts), true);
        }
        return changed;
    }

    // Returns the hearts of the player for a single target (usable with /execute store), otherwise the number of players queried
    private static int getPlayerHearts(CommandSourceStack source, Collection<ServerPlayer> targets) {
        int hearts = 0;
        int queried = 0;
        for (ServerPlayer serverPlayer : targets) {
            PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
            if (playerHearts == null) {
                continue;
            }
            hearts = 10 + playerHearts.getNumberOfHearts();
            int currentHearts = hearts;
            source.sendSuccess(() -> Component.translatableWithFallback("hardcorelite.command.get", "%s has %s", serverPlayer.getDisplayName(), PlayerHearts.heartsText(currentHearts)), false);
            queried++;
        }
        return targets.size() == 1 ? hearts : queried;
    }
}
