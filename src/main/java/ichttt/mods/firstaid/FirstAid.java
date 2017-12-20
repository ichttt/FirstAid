package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.*;
import ichttt.mods.firstaid.common.DebugDamageCommand;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(modid = FirstAid.MODID, name = FirstAid.NAME, version = "1.4.2", acceptedMinecraftVersions = "[1.12.2,1.13)", dependencies = "required-after:forge@[14.23.0.2526,);")
public class FirstAid {
    public static Logger logger;
    public static final String MODID = "firstaid";
    public static final String NAME = "First Aid";

    //RECEIVED CONFIG FIELDS
    public static FirstAidConfig.DamageSystem activeDamageConfig;
    public static FirstAidConfig.ExternalHealing activeHealingConfig;
    public static boolean scaleMaxHealth;
    public static int playerMaxHealth = -1;

    @SuppressWarnings("unused")
    @SidedProxy(clientSide = "ichttt.mods.firstaid.client.ClientProxy", serverSide = "ichttt.mods.firstaid.server.ServerProxy")
    public static IProxy proxy;

    public static CreativeTabs creativeTab;
    public static SimpleNetworkWrapper NETWORKING;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent pre) {
        logger = pre.getModLog();
        logger.debug("FirstAid starting");
        creativeTab = new CreativeTabs(FirstAid.MODID) {
            @Nonnull
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(FirstAidItems.BANDAGE, 1);
            }
        };

        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FirstAidItems.init();
        proxy.init();
        //Setup API
        RegistryManager.setupRegistries();
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.debug("Registering capability");
        CapabilityExtendedHealthSystem.register();

        logger.debug("Registering networking");
        int i = 0;
        NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORKING.registerMessage(MessageReceiveDamage.Handler.class, MessageReceiveDamage.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyHealingItem.Handler.class, MessageApplyHealingItem.class, ++i , Side.SERVER);
        NETWORKING.registerMessage(MessageReceiveConfiguration.Handler.class, MessageReceiveConfiguration.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyAbsorption.Handler.class, MessageApplyAbsorption.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageAddHealth.Handler.class, MessageAddHealth.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessagePlayHurtSound.Handler.class, MessagePlayHurtSound.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageClientUpdate.Handler.class, MessageClientUpdate.class, ++i, Side.SERVER);
        NETWORKING.registerMessage(MessageResync.Handler.class, MessageResync.class, ++i, Side.CLIENT);
        MessageReceiveConfiguration.validate();

        RegistryManager.registerDefaults();
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        RegistryManager.finalizeRegistries();
        checkEarlyExit();
    }

    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            logger.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

    @Mod.EventHandler
    public void beforeServerStart(FMLServerAboutToStartEvent event) {
        ICommandManager manager = event.getServer().getCommandManager();
        if (manager instanceof CommandHandler) {
            ((CommandHandler) manager).registerCommand(new DebugDamageCommand());
        }
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        logger.debug("Cleaning up");
        PlayerDataManager.capList.clear();
        PlayerDataManager.tutorialDone.clear();
        EventHandler.hitList.clear();
    }
}
