package net.petemc.hardcorelite.world;

import net.minecraft.world.level.gamerules.GameRule;

public class ModGamerules {
    public static GameRule<Boolean> CAN_RESTORE_HEARTS;

    public static GameRule<Integer> MAXIMUM_HEARTS;

    public static void createRules(){
        new ModGamerules();
    }
}
