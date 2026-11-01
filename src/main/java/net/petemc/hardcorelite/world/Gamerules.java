package net.petemc.hardcorelite.world;

import net.minecraft.world.level.GameRules;

public class Gamerules {
    public static final GameRules.Key<GameRules.BooleanValue> CAN_RESTORE_HEARTS = GameRules
            .register("canRestoreHearts", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    // Only has an effect while canRestoreHearts is true
    public static final GameRules.Key<GameRules.BooleanValue> GOLDEN_APPLE_RESTORES_HEARTS = GameRules
            .register("goldenAppleRestoresHearts", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    public static final GameRules.Key<GameRules.IntegerValue> MAXIMUM_HEARTS = GameRules
            .register("maxHearts", GameRules.Category.PLAYER, GameRules.IntegerValue.create(20));

    // 0 = players become spectators when they lose their last heart, > 0 = players never drop below this many hearts
    public static final GameRules.Key<GameRules.IntegerValue> MINIMUM_HEARTS = GameRules
            .register("minHearts", GameRules.Category.PLAYER, GameRules.IntegerValue.create(0));

    // minHearts clamped to 0..maxHearts, so invalid gamerule values can't break the heart logic
    public static int getMinimumHearts(GameRules gameRules) {
        return Math.max(0, Math.min(gameRules.getInt(MINIMUM_HEARTS), gameRules.getInt(MAXIMUM_HEARTS)));
    }

    // Called from the mod constructor so the static fields above register the gamerules early
    public static void init() {
    }
}
