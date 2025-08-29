package net.petemc.hardcorelite;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.petemc.hardcorelite.world.Gamerules;
import org.slf4j.Logger;

@Mod(HardcoreLite.MOD_ID)
public class HardcoreLite
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "hardcorelite";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public HardcoreLite(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Gamerules());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
}
