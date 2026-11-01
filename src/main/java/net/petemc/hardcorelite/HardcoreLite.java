package net.petemc.hardcorelite;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

// Gamerules are registered in ModEvents (RegisterEvent), all other handlers are in ModEvents too
@Mod(HardcoreLite.MOD_ID)
public class HardcoreLite {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "hardcorelite";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
}
