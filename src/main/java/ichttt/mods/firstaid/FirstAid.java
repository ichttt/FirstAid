/*
 * FirstAid
 * Copyright (C) 2017-2018
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
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.HealingItemApiHelperImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.util.MorpheusHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(FirstAid.MODID)
public class FirstAid {
    public static final String MODID = "firstaid";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final ItemGroup ITEM_GROUP = new ItemGroup(FirstAid.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(FirstAidItems.BANDAGE);
        }
    };

    private static final String NETWORKING_MAJOR = "1.";
    private static final String NETWORKING_MINOR = "0";

    private static final String NETWORKING_VERSION = NETWORKING_MAJOR + NETWORKING_MINOR;
    public static final SimpleChannel NETWORKING = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "channel"),
            () -> NETWORKING_VERSION,
            s -> s.startsWith(NETWORKING_MAJOR),
            s -> s.equals(NETWORKING_VERSION));
    public static boolean morpheusLoaded = false;


    public FirstAid() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        IEventBus bus = FMLModLoadingContext.get().getModEventBus();
        bus.addListener(this::preInit);
        bus.addListener(this::init);
        bus.addListener(this::loadComplete);
        bus.addListener(this::beforeServerStart);
        bus.addListener(this::onServerStop);
    }

    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} starting...", MODID);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        //Setup API
        HealingItemApiHelperImpl.init();
        RegistryManager.setupRegistries();
    }

    public void init(FMLInitializationEvent event) {
        CapabilityManager.INSTANCE.register(AbstractPlayerDamageModel.class, new Capability.IStorage<AbstractPlayerDamageModel>() {
                    @Nullable
                    @Override
                    public INBTBase writeNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side, INBTBase nbt) {
                        instance.deserializeNBT((NBTTagCompound) nbt);
                    }
                }
                , () -> {
                    throw new UnsupportedOperationException("No default implementation");
                });

//        int i = 0;
//        NETWORKING.registerMessage(MessageReceiveDamage.Handler.class, MessageReceiveDamage.class, ++i, Side.CLIENT);
//        NETWORKING.registerMessage(MessageApplyHealingItem.Handler.class, MessageApplyHealingItem.class, ++i , Side.SERVER);
//        NETWORKING.registerMessage(MessageConfiguration.Handler.class, MessageConfiguration.class, ++i, Side.CLIENT);
//        NETWORKING.registerMessage(MessageApplyAbsorption.Handler.class, MessageApplyAbsorption.class, ++i, Side.CLIENT);
//        NETWORKING.registerMessage(MessageAddHealth.Handler.class, MessageAddHealth.class, ++i, Side.CLIENT);
//        NETWORKING.registerMessage(MessagePlayHurtSound.Handler.class, MessagePlayHurtSound.class, ++i, Side.CLIENT);
//        NETWORKING.registerMessage(MessageClientRequest.Handler.class, MessageClientRequest.class, ++i, Side.SERVER);
//        NETWORKING.registerMessage(MessageSyncDamageModel.Handler.class, MessageSyncDamageModel.class, ++i, Side.CLIENT);


        if (ModList.get().isLoaded("morpheus")) {
            MorpheusHelper.register();
            morpheusLoaded = true;
        }

        RegistryManager.registerDefaults();
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        RegistryManager.finalizeRegistries();
    }

    public void beforeServerStart(FMLServerStartingEvent event) {
        //TODO commands
//        event.registerServerCommand(new DebugDamageCommand());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) {
        LOGGER.debug("Cleaning up");
        CapProvider.tutorialDone.clear();
        EventHandler.hitList.clear();
    }
}
