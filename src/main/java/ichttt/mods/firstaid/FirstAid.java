/*
 * FirstAid
 * Copyright (C) 2017-2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.DebugDamageCommand;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.apiimpl.HealingItemApiHelperImpl;
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
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(modid = FirstAid.MODID,
     name = FirstAid.NAME,
     version = FirstAid.VERSION,
     acceptedMinecraftVersions = "[1.12.2,1.13)",
     dependencies = "required-after:forge@[14.23.5.2803,);",
     guiFactory = "ichttt.mods.firstaid.client.config.GuiFactory",
     certificateFingerprint = FirstAid.FINGERPRINT)
public class FirstAid {
    public static final String MODID = "firstaid";
    public static final String NAME = "First Aid";
    public static final String VERSION = "1.6.18";
    public static final String FINGERPRINT = "7904c4e13947c8a616c5f39b26bdeba796500722";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static boolean isSynced = false;
    public static List<ConfigEntry<ExtraConfig.Sync>> syncedConfigOptions;

    @SidedProxy(clientSide = "ichttt.mods.firstaid.client.ClientProxy", serverSide = "ichttt.mods.firstaid.server.ServerProxy")
    public static IProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(FirstAid.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(FirstAidItems.BANDAGE);
        }
    };
    public static SimpleNetworkWrapper NETWORKING;
    public static boolean morpheusLoaded = false;

    static {
        File configDir = Loader.instance().getConfigDir();
        Path oldConfigPath = configDir.toPath().resolve(NAME + ".cfg");
        Path newConfigPath = configDir.toPath().resolve(MODID + ".cfg");
        if (Files.exists(oldConfigPath) && !Files.isDirectory(oldConfigPath) && !Files.exists(newConfigPath)) {
            try {
                Files.move(oldConfigPath, newConfigPath);
                LOGGER.info("Moved config file to new home");
            } catch (IOException e) {
                LOGGER.error("Failed to move config file to new correct location!", e);
            }
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} version {} starting", NAME, VERSION);
        if (FirstAidConfig.debug) {
            LOGGER.warn("DEBUG MODE ENABLED");
            LOGGER.warn("FirstAid may be slower than usual and will produce much noisier logs if debug mode is enabled");
            LOGGER.warn("Disable debug in firstaid config");
        }
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        proxy.preInit();
        //Setup API
        HealingItemApiHelperImpl.init();
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
        ExtraConfigManager.scheduleDelete("Overlay.mode");
        ExtraConfigManager.scheduleDelete("ExternalHealing.sleepHealing");
        ExtraConfigManager.scheduleDelete("experimentalSetHealth");
        ExtraConfigManager.scheduleDelete("Overlay.hideOnNoChange");
        ExtraConfigManager.scheduleDelete("Overlay.showVanillaHealthBar");
        ExtraConfigManager.postProcessConfigs();

        int i = 0;
        NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORKING.registerMessage(MessageUpdatePart.Handler.class, MessageUpdatePart.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyHealingItem.Handler.class, MessageApplyHealingItem.class, ++i , Side.SERVER);
        NETWORKING.registerMessage(MessageConfiguration.Handler.class, MessageConfiguration.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageApplyAbsorption.Handler.class, MessageApplyAbsorption.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageAddHealth.Handler.class, MessageAddHealth.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessagePlayHurtSound.Handler.class, MessagePlayHurtSound.class, ++i, Side.CLIENT);
        NETWORKING.registerMessage(MessageClientRequest.Handler.class, MessageClientRequest.class, ++i, Side.SERVER);
        NETWORKING.registerMessage(MessageSyncDamageModel.Handler.class, MessageSyncDamageModel.class, ++i, Side.CLIENT);

        proxy.init();

        if (Loader.isModLoaded("morpheus")) {
            MorpheusHelper.register();
            morpheusLoaded = true;
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
        if (!event.getExpectedFingerprint().equals(FINGERPRINT)) return;
        if (event.getFingerprints().isEmpty()) {
            LOGGER.warn("NO VALID FINGERPRINT FOR FIRST AID! EXPECTED " + event.getExpectedFingerprint() + " BUT FOUND NONE!");
        } else {
            LOGGER.warn("FOUND AN INVALID FINGERPRINT FOR FIRST AID! EXPECTED " + event.getExpectedFingerprint() + " BUT GOT THE FOLLOWING:");
            for (String fingerprint : event.getFingerprints()) {
                LOGGER.warn(fingerprint);
            }
        }
        LOGGER.warn("THIS IS NOT AN OFFICIAL BUILD OF FIRST AID!");
        LOGGER.warn("Please download the official version from CurseForge");
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
