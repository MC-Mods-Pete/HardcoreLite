package net.petemc.hardcorelite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
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
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("playerhearts")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("get")
                        .executes(command -> getPlayerHearts(command.getSource(), List.of(command.getSource().getPlayerOrThrow())))
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .executes(command -> getPlayerHearts(command.getSource(), EntityArgumentType.getPlayers(command, "targets")))))
                .then(CommandManager.literal("set").then(setArguments()))
                .then(setArguments()));
    }

    // Shared by "set" and the short form
    private static RequiredArgumentBuilder<ServerCommandSource, Integer> setArguments() {
        return CommandManager.argument("numberHearts", IntegerArgumentType.integer(1))
                .executes(command -> setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"),
                        List.of(command.getSource().getPlayerOrThrow())))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes(command -> setPlayerHearts(command.getSource(), IntegerArgumentType.getInteger(command, "numberHearts"),
                                EntityArgumentType.getPlayers(command, "targets"))));
    }

    private static int getPlayerHearts(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        int hearts = 0;
        for (ServerPlayerEntity serverPlayer : targets) {
            hearts = 10 + StateSaverAndLoader.getPlayerHearts(serverPlayer).getNumberOfHearts();
            Text text = Text.translatableWithFallback("hardcorelite.command.get", "%s has %s", serverPlayer.getDisplayName(), PlayerHearts.heartsText(hearts));
            source.sendFeedback(() -> text, false);
        }
        // A single target returns its hearts, so the value can be used with /execute store
        return targets.size() == 1 ? hearts : targets.size();
    }

    private static int setPlayerHearts(ServerCommandSource source, int numberHearts, Collection<ServerPlayerEntity> targets) {
        GameRules gameRules = source.getServer().getGameRules();
        int minHearts = Math.max(1, ModGamerules.getMinimumHearts(gameRules));
        int maxHearts = gameRules.getInt(ModGamerules.MAXIMUM_HEARTS);
        if (numberHearts < minHearts || numberHearts > maxHearts) {
            source.sendError(Text.translatableWithFallback("hardcorelite.command.set.invalid_value",
                    "Value needs to be between %s and %s", minHearts, maxHearts));
            return 0;
        }

        for (ServerPlayerEntity serverPlayer : targets) {
            PlayerHearts playerHearts = StateSaverAndLoader.getPlayerHearts(serverPlayer);
            playerHearts.setNumberOfHearts(numberHearts - 10);
            Objects.requireNonNull(serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(20 + playerHearts.getNumberOfHearts() * 2);
            serverPlayer.setHealth(20 + playerHearts.getNumberOfHearts() * 2);
            // Like vanilla /gamemode: tell the target, if someone else changed its hearts
            if (source.getEntity() != serverPlayer && gameRules.getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                serverPlayer.sendMessage(Text.translatableWithFallback("hardcorelite.command.set.target",
                        "Your hearts have been set to %s", numberHearts));
            }
        }

        if (targets.size() == 1) {
            Text name = targets.iterator().next().getDisplayName();
            source.sendFeedback(() -> Text.translatableWithFallback("hardcorelite.command.set.single",
                    "Changed current hearts of %s to %s", name, numberHearts), true);
        } else {
            source.sendFeedback(() -> Text.translatableWithFallback("hardcorelite.command.set.multiple",
                    "Changed current hearts of %s players to %s", targets.size(), numberHearts), true);
        }
        return targets.size();
    }
}
