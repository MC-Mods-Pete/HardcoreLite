package net.petemc.hardcorelite.world;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;

public class ModGamerules {

    public static final GameRule<Boolean> CAN_RESTORE_HEARTS =
            GameRuleBuilder.forBoolean(true)
                    .buildAndRegister(Identifier.ofVanilla("can_restore_hearts"));

    public static final GameRule<Integer> MAXIMUM_HEARTS =
            GameRuleBuilder.forInteger(20).minValue(0)
                    .buildAndRegister(Identifier.ofVanilla("max_hearts"));

    public static void createRules(){
        new ModGamerules();
    }
}

