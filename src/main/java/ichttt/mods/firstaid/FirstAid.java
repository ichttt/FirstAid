package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.DebugDamageCommand;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientUpdate;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.common.network.MessageReceiveConfiguration;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.network.MessageResync;
import ichttt.mods.firstaid.common.util.MorpheusHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mod(modid = FirstAid.MODID,
     name = FirstAid.NAME,
     version = FirstAid.VERSION,
     acceptedMinecraftVersions = "[1.12.2,1.13)",
     dependencies = "required-after:forge@[14.23.0.2526,);",
     guiFactory = "ichttt.mods.firstaid.client.config.GuiFactory")
public class FirstAid {
    public static Logger logger;
    public static final String MODID = "firstaid";
    public static final String NAME = "First Aid";
    public static final String VERSION = "1.5.2";

    public static boolean isSynced = false;
    public static List<ConfigEntry<ExtraConfig.Sync>> syncedConfigOptions;

    @SuppressWarnings("unused")
    @SidedProxy(clientSide = "ichttt.mods.firstaid.client.ClientProxy", serverSide = "ichttt.mods.firstaid.server.ServerProxy")
    public static IProxy proxy;

    public static CreativeTabs creativeTab;
    public static SimpleNetworkWrapper NETWORKING;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent pre) {
        logger = pre.getModLog();
        logger.debug("FirstAid version {} starting", VERSION);
        creativeTab = new CreativeTabs(FirstAid.MODID) {
            @Nonnull
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(FirstAidItems.BANDAGE);
            }
        };

        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FirstAidItems.init();
        proxy.preInit();
        //Setup API
        RegistryManager.setupRegistries();
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        CapabilityManager.INSTANCE.register(AbstractPlayerDamageModel.class, new Capability.IStorage<AbstractPlayerDamageModel>() {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side, NBTBase nbt) {
                        instance.deserializeNBT((NBTTagCompound) nbt);
                    }
                }
                , () -> {
                    try {
                        throw new UnsupportedOperationException("No default implementation");
                    } catch (UnsupportedOperationException e) { //TODO remove catch
                        FirstAid.logger.warn("A mod still uses the default implementation of the capability. This is deprecated and will be removed in feature releases", e);
                    }
                    return new CapabilityExtendedHealthSystem.DefaultImpl();
                });
        syncedConfigOptions = ExtraConfigManager.getAnnotatedFields(ExtraConfig.Sync.class, FirstAidConfig.class);

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

        proxy.init();

        if (Loader.isModLoaded("morpheus")) {
            logger.info("Morpheus present - enabling compatibility module");
            MorpheusHelper.register();
        }

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
    public void beforeServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new DebugDamageCommand());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        logger.debug("Cleaning up");
        PlayerDataManager.clear();
        EventHandler.hitList.clear();
    }
}
