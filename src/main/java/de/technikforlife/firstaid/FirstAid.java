package de.technikforlife.firstaid;

import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.capability.DataManager;
import de.technikforlife.firstaid.items.FirstAidItems;
import de.technikforlife.firstaid.network.MessageApplyHealth;
import de.technikforlife.firstaid.network.MessageGetDamageInfo;
import de.technikforlife.firstaid.network.MessageReceiveDamageInfo;
import de.technikforlife.firstaid.network.MessageReceiveDamageInfoWithItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = FirstAid.MODID, name = FirstAid.NAME, version = "1.1.0", acceptedMinecraftVersions = "[1.12,1.13)")
public class FirstAid {
    public static Logger logger;
    public static final String MODID ="firstaid";
    public static final String NAME ="First Aid";

    @SuppressWarnings("unused")
    @SidedProxy(clientSide = "de.technikforlife.firstaid.client.ClientProxy", serverSide = "de.technikforlife.firstaid.server.ServerProxy")
    public static IProxy proxy;

    public static CreativeTabFirstAid creativeTab;
    public static SimpleNetworkWrapper NETWORKING;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent pre) {
        logger = pre.getModLog();
        logger.debug("FirstAid starting");
        creativeTab = new CreativeTabFirstAid();
        FirstAidItems.init();
        proxy.init();
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        CapabilityExtendedHealthSystem.register();
        NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORKING.registerMessage(MessageReceiveDamageInfoWithItem.Handler.class, MessageReceiveDamageInfoWithItem.class, 1, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyHealth.Handler.class, MessageApplyHealth.class, 2 , Side.SERVER);
        NETWORKING.registerMessage(MessageReceiveDamageInfo.Handler.class, MessageReceiveDamageInfo.class, 3, Side.CLIENT);
        NETWORKING.registerMessage(MessageGetDamageInfo.Handler.class, MessageGetDamageInfo.class, 4, Side.SERVER);
        checkEarlyExit();
    }

    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            logger.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

    @Mod.EventHandler
    public void onStop(FMLServerStoppedEvent event) {
        logger.debug("Cleaning up");
        DataManager.clearData();
    }
}
