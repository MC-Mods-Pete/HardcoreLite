package net.petemc.hardcorelite.world;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

// Since 1.21.11 gamerules have snake_case ids. Like the two rules from 1.0.0 the new ones use the default (minecraft) namespace,
// so all of them work as /gamerule <name>. Don't rename existing ids, worlds would lose their values.
public class ModGamerules {

    public static final GameRule<Boolean> CAN_RESTORE_HEARTS =
            GameRuleBuilder.forBoolean(true)
                    .buildAndRegister(Identifier.ofVanilla("can_restore_hearts"));

    public static final GameRule<Integer> MAXIMUM_HEARTS =
            GameRuleBuilder.forInteger(20).minValue(0)
                    .buildAndRegister(Identifier.ofVanilla("max_hearts"));

    public static final GameRule<Integer> MINIMUM_HEARTS =
            GameRuleBuilder.forInteger(0).minValue(0)
                    .buildAndRegister(Identifier.ofVanilla("min_hearts"));

    public static final GameRule<Boolean> GOLDEN_APPLE_RESTORES_HEARTS =
            GameRuleBuilder.forBoolean(false)
                    .buildAndRegister(Identifier.ofVanilla("golden_apple_restores_hearts"));

    // Negative values count as 0, the minimum can't be above maxHearts
    public static int getMinimumHearts(GameRules gameRules) {
        return Math.max(0, Math.min(gameRules.getValue(MINIMUM_HEARTS), gameRules.getValue(MAXIMUM_HEARTS)));
    }

    public static void createRules(){
        new ModGamerules();
    }
}
