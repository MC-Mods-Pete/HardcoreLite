package mars.somewhathardcore.com;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SomewhatHardcore.MOD_ID)
public class SomewhatHardcore
{
    public static final String MOD_ID = "somewhathardcore";
    public SomewhatHardcore()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Gamerules());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
}
