/*
 * FirstAid
 * Copyright (C) 2017-2023
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
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.RegistryObjects;
import ichttt.mods.firstaid.common.apiimpl.HealingItemApiHelperImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.compat.playerrevive.PRCompatManager;
import ichttt.mods.firstaid.common.network.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FirstAid.MODID)
public class FirstAid {
    public static final String MODID = "firstaid";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final String NETWORKING_MAJOR = "3.";
    private static final String NETWORKING_MINOR = "0";

    private static final String NETWORKING_VERSION = NETWORKING_MAJOR + NETWORKING_MINOR;
    public static final SimpleChannel NETWORKING = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "channel"),
            () -> NETWORKING_VERSION,
            s -> s.startsWith(NETWORKING_MAJOR),
            s -> s.equals(NETWORKING_VERSION));
    public static boolean isSynced = false;
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public FirstAid() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);
        bus.addListener(this::registerCapability);
        CREATIVE_MODE_TABS.register(bus);
        RegistryObjects.registerToBus(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, FirstAidConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FirstAidConfig.generalSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FirstAidConfig.clientSpec);

        //noinspection Convert2MethodRef
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.setup());
        //Setup API
        HealingItemApiHelperImpl.init();
        RegistryManager.registerAndValidate();
    }

    public static final RegistryObject<CreativeModeTab> FIRST_AID = CREATIVE_MODE_TABS.register("first_aid", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RegistryObjects.BANDAGE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RegistryObjects.BANDAGE.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(RegistryObjects.PLASTER.get());
                output.accept(RegistryObjects.MORPHINE.get());
            }).build());

    //@SuppressWarnings("Convert2MethodRef") //Fucking classloading
    public void init(FMLCommonSetupEvent event) {
        LOGGER.info("{} starting...", MODID);
        if (FirstAidConfig.GENERAL.debug.get()) {
            LOGGER.warn("DEBUG MODE ENABLED");
            LOGGER.warn("FirstAid may be slower than usual and will produce much noisier logs if debug mode is enabled");
            LOGGER.warn("Disable debug in firstaid config");
        }

        int i = 0;
        NETWORKING.registerMessage(++i, MessageUpdatePart.class, MessageUpdatePart::encode, MessageUpdatePart::new, (message, supplier) -> MessageUpdatePart.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageApplyHealingItem.class, MessageApplyHealingItem::encode, MessageApplyHealingItem::new, (message, supplier) -> MessageApplyHealingItem.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageConfiguration.class, MessageConfiguration::encode, MessageConfiguration::new, (message, supplier) -> MessageConfiguration.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageApplyAbsorption.class, MessageApplyAbsorption::encode, MessageApplyAbsorption::new, (message, supplier) -> MessageApplyAbsorption.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageAddHealth.class, MessageAddHealth::encode, MessageAddHealth::new, (message, supplier) -> MessageAddHealth.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessagePlayHurtSound.class, MessagePlayHurtSound::encode, MessagePlayHurtSound::new, (message, supplier) -> MessagePlayHurtSound.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageClientRequest.class, MessageClientRequest::encode, MessageClientRequest::new, (message, supplier) -> MessageClientRequest.Handler.onMessage(message, supplier));
        NETWORKING.registerMessage(++i, MessageSyncDamageModel.class, MessageSyncDamageModel::encode, MessageSyncDamageModel::new, (message, supplier) -> MessageSyncDamageModel.Handler.onMessage(message, supplier));

        event.enqueueWork(() -> PRCompatManager.init());
    }

    public void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(AbstractPlayerDamageModel.class);
    }
}
