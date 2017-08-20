package de.technikforlife.firstaid;

import de.technikforlife.firstaid.items.FirstAidItems;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = FirstAid.MODID, name = FirstAid.NAME, version = "0.0.1", acceptedMinecraftVersions = "[1.12,1.13)")
public class FirstAid {
    public static Logger logger;
    public static final String MODID ="firstaid";
    public static final String NAME ="First Aid";
    private static final String NREG ="naturalRegeneration";
    @SidedProxy(clientSide = "de.technikforlife.firstaid.client.ClientProxy", serverSide = "de.technikforlife.firstaid.server.ServerProxy")
    public static IProxy proxy;
    public static CreativeTabFirstAid creativeTab;


    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent pre) {
        logger = pre.getModLog();
        logger.info("First Aid loaded, need HELP!");
        creativeTab = new CreativeTabFirstAid();
        FirstAidItems.init();
        proxy.init();
    }

    @Mod.EventHandler
    public void serstart(FMLServerStartedEvent args) {
        for (World world:DimensionManager.getWorlds()) {
            world.getGameRules().setOrCreateGameRule(NREG, Boolean.toString(FirstAidConfig.allowNaturalRegeneration));
        }
        logger.info("Gamerule: {} set to {}.", NREG, Boolean.toString(FirstAidConfig.allowNaturalRegeneration));
    }
}
