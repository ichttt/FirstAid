package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.damagesystem.PartHealer;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.debuff.Debuffs;
import ichttt.mods.firstaid.items.FirstAidItems;
import ichttt.mods.firstaid.network.*;
import ichttt.mods.firstaid.util.DebugDamageCommand;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        FirstAidItems.init();
        proxy.init();
        //Setup API
        FirstAidRegistry.setImpl(FirstAidRegistryImpl.INSTANCE);
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

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
        NETWORKING.registerMessage(MessageHasTutorial.Handler.class, MessageHasTutorial.class, ++i, Side.SERVER);
        MessageReceiveConfiguration.validate();

        logger.debug("Registering defaults registry values");
        FirstAidRegistry registry = Objects.requireNonNull(FirstAidRegistry.getImpl());

        registry.bindHealingType(EnumHealingType.BANDAGE, type -> new PartHealer(400, 3, type));
        registry.bindHealingType(EnumHealingType.PLASTER, type -> new PartHealer(500, 2, type));

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> feetList = new ArrayList<>(2);
        feetList.add(Pair.of(EntityEquipmentSlot.FEET, new EnumPlayerPart[]{EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT}));
        feetList.add(Pair.of(EntityEquipmentSlot.LEGS, new EnumPlayerPart[]{EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG}));
        registry.bindDamageSourceStandard("fall", feetList);
        registry.bindDamageSourceStandard("hotFloor", feetList);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headList = new ArrayList<>(1);
        headList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        registry.bindDamageSourceStandard("anvil", headList);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> bodyList = new ArrayList<>(1);
        bodyList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
        registry.bindDamageSourceStandard("starve", bodyList);

        registry.bindDamageSourceRandom("magic", false, false);
        registry.bindDamageSourceRandom("drown", false, true);
        registry.bindDamageSourceRandom("inWall", false, true);

        logger.debug("Initializing debuffs");
        //noinspection ResultOfMethodCallIgnored
        Debuffs.getArmDebuffs().toString();
        logger.info("Initialized 4 debuffs");
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        FirstAidRegistryImpl.verify();
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
