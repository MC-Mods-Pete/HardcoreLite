package net.petemc.hardcorelite.world;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

// The rules are registered in ModEvents.registerGamerules (RegisterEvent), with snake_case ids in the minecraft namespace
// like in Fabric 26.1. Don't rename existing ids, worlds would lose their values.
public class ModGamerules {
    public static GameRule<Boolean> CAN_RESTORE_HEARTS;

    public static GameRule<Integer> MAXIMUM_HEARTS;

    public static GameRule<Integer> MINIMUM_HEARTS;

    public static GameRule<Boolean> GOLDEN_APPLE_RESTORES_HEARTS;

    // Negative values count as 0, the minimum can't be above maxHearts
    public static int getMinimumHearts(GameRules gameRules) {
        return Math.max(0, Math.min(gameRules.get(MINIMUM_HEARTS), gameRules.get(MAXIMUM_HEARTS)));
    }
}
