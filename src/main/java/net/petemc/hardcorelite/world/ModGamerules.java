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

    public static void createRules(){
        new ModGamerules();
    }
}

