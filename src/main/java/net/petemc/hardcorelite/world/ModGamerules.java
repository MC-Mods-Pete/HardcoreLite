package net.petemc.hardcorelite.world;

import net.minecraft.world.level.GameRules;

public class ModGamerules {
    public static final GameRules.Key<GameRules.BooleanValue> CAN_RESTORE_HEARTS = GameRules
            .register("canRestoreHearts", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.IntegerValue> MAXIMUM_HEARTS = GameRules
            .register("maxHearts", GameRules.Category.PLAYER, GameRules.IntegerValue.create(20));

    public static void createRules(){
        new ModGamerules();
    }
}
