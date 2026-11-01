package net.petemc.hardcorelite.world;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class ModGamerules {
    public ModGamerules(){}

    public static final GameRules.Key<GameRules.BooleanRule> CAN_RESTORE_HEARTS = GameRuleRegistry
            .register("canRestoreHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    public static final GameRules.Key<GameRules.IntRule> MAXIMUM_HEARTS = GameRuleRegistry
            .register("maxHearts", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(20));

    public static final GameRules.Key<GameRules.IntRule> MINIMUM_HEARTS = GameRuleRegistry
            .register("minHearts", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0));

    public static final GameRules.Key<GameRules.BooleanRule> GOLDEN_APPLE_RESTORES_HEARTS = GameRuleRegistry
            .register("goldenAppleRestoresHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    // Negative values count as 0, the minimum can't be above maxHearts
    public static int getMinimumHearts(GameRules gameRules) {
        return Math.max(0, Math.min(gameRules.getInt(MINIMUM_HEARTS), gameRules.getInt(MAXIMUM_HEARTS)));
    }

    public static void createRules(){
        new ModGamerules();
    }
}
