package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.DebugDamageCommand;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
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
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mod(modid = FirstAid.MODID,
     name = FirstAid.NAME,
     version = FirstAid.VERSION,
     acceptedMinecraftVersions = "[1.12.2,1.13)",
     dependencies = "required-after:forge@[14.23.4.2724,);",
     guiFactory = "ichttt.mods.firstaid.client.config.GuiFactory",
     certificateFingerprint = "7904c4e13947c8a616c5f39b26bdeba796500722")
public class FirstAid {
    public static final String MODID = "firstaid";
    public static final String NAME = "First Aid";
    public static final String VERSION = "1.5.6";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static boolean isSynced = false;
    public static List<ConfigEntry<ExtraConfig.Sync>> syncedConfigOptions;

    @SidedProxy(clientSide = "ichttt.mods.firstaid.client.ClientProxy", serverSide = "ichttt.mods.firstaid.server.ServerProxy")
    public static IProxy proxy;

    public static CreativeTabs creativeTab;
    public static SimpleNetworkWrapper NETWORKING;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("FirstAid version {} starting", VERSION);
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
                    throw new UnsupportedOperationException("No default implementation");
                });
        syncedConfigOptions = ExtraConfigManager.getAnnotatedFields(ExtraConfig.Sync.class, FirstAidConfig.class);
        ExtraConfigManager.scheduleDelete("Overlay.hudScale");
        ExtraConfigManager.scheduleDelete("Overlay.displayHealthAsNumber");
        ExtraConfigManager.scheduleDelete("Overlay.heartThreshold");
        ExtraConfigManager.scheduleDelete("Overlay.showOverlay");
        ExtraConfigManager.scheduleDelete("Overlay.onlyShowWhenDamaged");
        ExtraConfigManager.scheduleDelete("Overlay.position");
        ExtraConfigManager.postProccessConfigs();

        int i = 0;
        NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORKING.registerMessage(MessageReceiveDamage.Handler.class, MessageReceiveDamage.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyHealingItem.Handler.class, MessageApplyHealingItem.class, ++i , Side.SERVER);
        NETWORKING.registerMessage(MessageConfiguration.Handler.class, MessageConfiguration.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyAbsorption.Handler.class, MessageApplyAbsorption.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageAddHealth.Handler.class, MessageAddHealth.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessagePlayHurtSound.Handler.class, MessagePlayHurtSound.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageClientRequest.Handler.class, MessageClientRequest.class, ++i, Side.SERVER);
        NETWORKING.registerMessage(MessageSyncDamageModel.Handler.class, MessageSyncDamageModel.class, ++i, Side.CLIENT);

        proxy.init();

        if (Loader.isModLoaded("morpheus")) {
            LOGGER.info("Morpheus present - enabling compatibility module");
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

    @Mod.EventHandler
    public void wrongFingerprint(FMLFingerprintViolationEvent event) {
        if (event.getFingerprints().isEmpty()) {
            LOGGER.error("NO VALID FINGERPRINT FOR FIRST AID! EXPECTED " + event.getExpectedFingerprint() + " BUT FOUND NONE!");
        } else {
            LOGGER.error("FOUND AN INVALID FINGERPRINT FOR FIRST AID! EXPECTED " + event.getExpectedFingerprint() + " BUT GOT THE FOLLOWING:");
            for (String fingerprint : event.getFingerprints()) {
                LOGGER.error(fingerprint);
            }
        }
        LOGGER.error("THIS IS NOT AN OFFICIAL BUILD OF FIRST AID!");
        LOGGER.error("Please download the official version from CurseForge");
    }

    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            LOGGER.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

    @Mod.EventHandler
    public void beforeServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new DebugDamageCommand());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        LOGGER.debug("Cleaning up");
        CapProvider.tutorialDone.clear();
        EventHandler.hitList.clear();
    }
}
