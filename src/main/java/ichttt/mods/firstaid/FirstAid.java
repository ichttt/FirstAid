package ichttt.mods.firstaid;

import ichttt.mods.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.debuff.Debuffs;
import ichttt.mods.firstaid.items.FirstAidItems;
import ichttt.mods.firstaid.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.network.MessageApplyHealth;
import ichttt.mods.firstaid.network.MessageReceiveDamage;
import ichttt.mods.firstaid.network.MessageReceiveDamageModel;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = FirstAid.MODID, name = FirstAid.NAME, version = "1.2.3", acceptedMinecraftVersions = "[1.12,1.13)")
public class FirstAid {
    public static Logger logger;
    public static final String MODID ="firstaid";
    public static final String NAME ="First Aid";

    @SuppressWarnings("unused")
    @SidedProxy(clientSide = "ichttt.mods.firstaid.client.ClientProxy", serverSide = "ichttt.mods.firstaid.server.ServerProxy")
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
        logger.debug("Registering networking");
        NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORKING.registerMessage(MessageReceiveDamage.Handler.class, MessageReceiveDamage.class, 1, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyHealth.Handler.class, MessageApplyHealth.class, 2 , Side.SERVER);
        NETWORKING.registerMessage(MessageReceiveDamageModel.Handler.class, MessageReceiveDamageModel.class, 3, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyAbsorption.Handler.class, MessageApplyAbsorption.class, 4, Side.CLIENT);
        logger.debug("Initializing debuffs");
        //noinspection ResultOfMethodCallIgnored
        Debuffs.getArmDebuffs().toString();
        logger.info("Initialized 4 debuffs");
        checkEarlyExit();
    }

    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            logger.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartedEvent event) {
        for (World world : DimensionManager.getWorlds())
            world.getGameRules().setOrCreateGameRule("naturalRegeneration", Boolean.toString(FirstAidConfig.externalHealing.allowNaturalRegeneration));
        logger.debug("Natural regeneration has been set to {}", FirstAidConfig.externalHealing.allowNaturalRegeneration);
    }

    @Mod.EventHandler
    public void onStop(FMLServerStoppedEvent event) {
        logger.debug("Cleaning up");
        PlayerDataManager.capList.clear();
    }
}
