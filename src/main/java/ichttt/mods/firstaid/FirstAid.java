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
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.HealingItemApiHelperImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.potion.FirstAidPotion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
    public static boolean isSynced = false;


    public FirstAid() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);
        bus.addListener(this::loadComplete);
        bus.addGenericListener(Item.class, this::registerItems);
        bus.addGenericListener(Potion.class, this::registerPotion);
        bus.addGenericListener(SoundEvent.class, this::registerSound);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientHooks::setup));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientHooks::lateSetup));
        //Setup API
        HealingItemApiHelperImpl.init();
        RegistryManager.setupRegistries();
    }

    public void init(FMLCommonSetupEvent event) {
        LOGGER.info("{} starting...", MODID);
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

        int i = 0;
        NETWORKING.registerMessage(++i, MessageReceiveDamage.class, MessageReceiveDamage::encode, MessageReceiveDamage::new, MessageReceiveDamage.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageApplyHealingItem.class, MessageApplyHealingItem::encode, MessageApplyHealingItem::new, MessageApplyHealingItem.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageConfiguration.class, MessageConfiguration::encode, MessageConfiguration::new, MessageConfiguration.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageApplyAbsorption.class, MessageApplyAbsorption::encode, MessageApplyAbsorption::new, MessageApplyAbsorption.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageAddHealth.class, MessageAddHealth::encode, MessageAddHealth::new, MessageAddHealth.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessagePlayHurtSound.class, MessagePlayHurtSound::encode, MessagePlayHurtSound::new, MessagePlayHurtSound.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageClientRequest.class, MessageClientRequest::encode, MessageClientRequest::new, MessageClientRequest.Handler::onMessage);
        NETWORKING.registerMessage(++i, MessageSyncDamageModel.class, MessageSyncDamageModel::encode, MessageSyncDamageModel::new, MessageSyncDamageModel.Handler::onMessage);


//        if (ModList.get().isLoaded("morpheus")) { TODO morpheus helper
//            MorpheusHelper.register();
//            morpheusLoaded = true;
//        }

        RegistryManager.registerDefaults();
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        RegistryManager.finalizeRegistries();
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        FirstAidItems.registerItems(event.getRegistry());
    }

    public void registerPotion(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(new FirstAidPotion(false, 0xDDD, FirstAidItems.MORPHINE).setBeneficial());
    }

    public void registerSound(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(new SoundEvent(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat")).setRegistryName(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat")));
    }
}
