/*
 * FirstAid
 * Copyright (C) 2017-2020
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

package ichttt.mods.firstaid.common;

import com.creativemd.playerrevive.api.event.PlayerKilledEvent;
import com.creativemd.playerrevive.api.event.PlayerRevivedEvent;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.PreferredDamageDistribution;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.potion.FirstAidPotion;
import ichttt.mods.firstaid.common.potion.PotionPoisonPatched;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.ProjectileHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;

public class EventHandler {
    public static final Random rand = new Random();
    public static final SoundEvent HEARTBEAT = new SoundEvent(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat")).setRegistryName(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat"));
    public static final Potion MORPHINE = new FirstAidPotion(false, 0xDDD, "morphine").setBeneficial();
    public static final Potion POISEN_PATCHED = PotionPoisonPatched.INSTANCE;

    public static final Map<EntityPlayer, Pair<Entity, RayTraceResult>> hitList = new WeakHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !(entity instanceof EntityPlayer) || entity instanceof FakePlayer)
            return;
        float amountToDamage = event.getAmount();
        EntityPlayer player = (EntityPlayer) entity;
        AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        DamageSource source = event.getSource();

        if (amountToDamage == Float.MAX_VALUE) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof EntityPlayerMP)
                FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(damageModel, false), (EntityPlayerMP) player);
            event.setCanceled(true);
            CommonUtils.killPlayer(damageModel, player, source);
            return;
        }

        boolean addStat = amountToDamage < 3.4028235E37F;
        IDamageDistribution damageDistribution = FirstAidRegistryImpl.INSTANCE.getDamageDistribution(source);

        if (source.isProjectile()) {
            Pair<Entity, RayTraceResult> rayTraceResult = hitList.remove(player);
            if (rayTraceResult != null) {
                Entity entityProjectile = rayTraceResult.getLeft();
                EntityEquipmentSlot slot = ProjectileHelper.getPartByPosition(entityProjectile, player);
                if (slot != null)
                    damageDistribution = new PreferredDamageDistribution(slot);
            }
        }
        DamageDistribution.handleDamageTaken(damageDistribution, damageModel, amountToDamage, player, source, addStat, true);

        event.setCanceled(true);

        hitList.remove(player);
    }

    @SubscribeEvent(priority =  EventPriority.LOWEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        RayTraceResult result = event.getRayTraceResult();
        if (result.typeOfHit != RayTraceResult.Type.ENTITY)
            return;

        Entity entity = result.entityHit;
        if (!entity.world.isRemote && entity instanceof EntityPlayer) {
            hitList.put((EntityPlayer) entity, Pair.of(event.getEntity(), event.getRayTraceResult()));
        }
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer)) {
            EntityPlayer player = (EntityPlayer) obj;
            AbstractPlayerDamageModel damageModel = PlayerDamageModel.create();
            event.addCapability(CapProvider.IDENTIFIER, new CapProvider(damageModel));
            //replace the data manager with our wrapper to grab absorption
            player.dataManager = new DataManagerWrapper(player, player.dataManager);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        FirstAidItems.registerItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerPotion(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(MORPHINE);
        event.getRegistry().register(POISEN_PATCHED);
    }

    @SubscribeEvent
    public static void registerSound(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(HEARTBEAT);
    }

    @SubscribeEvent
    public static void tickPlayers(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && CommonUtils.isSurvivalOrAdventure(event.player)) {
            Objects.requireNonNull(event.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).tick(event.player.world, event.player);
            hitList.remove(event.player); //Damage should be done in the same tick as the hit was noted, otherwise we got a false-positive
        }
    }

    @SubscribeEvent
    public static void tickWorld(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (FirstAidConfig.externalHealing.sleepHealPercentage <= 0D) return;
        World world = event.world;
        if (!world.isRemote && world instanceof WorldServer && ((WorldServer) world).areAllPlayersAsleep()) {
            for (EntityPlayer player : world.playerEntities) {
                AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
                Objects.requireNonNull(damageModel, "damage model").sleepHeal(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableName = event.getName();
        LootPool pool = null;
        int bandage = 0, plaster = 0, morphine = 0;
        if (tableName.equals(LootTableList.CHESTS_SPAWN_BONUS_CHEST)) {
            pool = event.getTable().getPool("main");
            bandage = 8;
            plaster = 16;
            morphine = 4;
        } else if (tableName.equals(LootTableList.CHESTS_STRONGHOLD_CORRIDOR) || tableName.equals(LootTableList.CHESTS_STRONGHOLD_CROSSING) || tableName.equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)) {
            pool = event.getTable().getPool("main");
            bandage = 20;
            plaster = 24;
            morphine = 8;
        }

        if (pool != null) {
            pool.addEntry(new LootEntryItem(FirstAidItems.BANDAGE, bandage, 0, new SetCount[]{new SetCount(new LootCondition[0], new RandomValueRange(1, 3))}, new LootCondition[0], FirstAid.MODID + "bandage"));
            pool.addEntry(new LootEntryItem(FirstAidItems.PLASTER, plaster, 0, new SetCount[]{new SetCount(new LootCondition[0], new RandomValueRange(1, 5))}, new LootCondition[0], FirstAid.MODID + "plaster"));
            pool.addEntry(new LootEntryItem(FirstAidItems.MORPHINE, morphine, 0, new SetCount[]{new SetCount(new LootCondition[0], new RandomValueRange(1, 2))}, new LootCondition[0], FirstAid.MODID + "morphine"));
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(FirstAid.MODID)) {
            Map<ConfigEntry<ExtraConfig.Sync>, ByteBuf> map = new LinkedHashMap<>();

            for (ConfigEntry<ExtraConfig.Sync> option : FirstAid.syncedConfigOptions) {
                if (option.hasRemoteData()) { //if we have remote data, we must make sure to revert it before saving to file
                    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
                    option.writeToBuf(buf);
                    map.put(option, buf); //we still have to keep the value to put it back after saving to file
                    option.revert(); //revert to client default state
                }
            }

            ConfigManager.sync(FirstAid.MODID, Config.Type.INSTANCE); //sync to file

            for (ConfigEntry<ExtraConfig.Sync> option : FirstAid.syncedConfigOptions) {
                option.updateOrigState(); //make sure we revert to the correct value again
                if (map.containsKey(option)) {
                    ByteBuf buf = map.get(option);
                    option.readFromBuf(buf); //put back the old remote value if we have been connected
                    buf.release();
                }
            }
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.hasCapability(CapabilityExtendedHealthSystem.INSTANCE, null))
            return;
        event.setCanceled(true);
        if (entity.world.isRemote || !FirstAidConfig.externalHealing.allowOtherHealingItems)
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodStats.class.getName()))) {
            if (FirstAidConfig.externalHealing.allowNaturalRegeneration)
                amount = amount * (float) FirstAidConfig.externalHealing.naturalRegenMultiplier;
        } else {
            amount = amount * (float) FirstAidConfig.externalHealing.otherRegenMultiplier;
        }
        if (FirstAidConfig.debug) {
            CommonUtils.debugLogStacktrace("External healing: : " + amount);
        }
        HealthDistribution.distributeHealth(amount, (EntityPlayer) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            FirstAid.LOGGER.debug("Sending damage model to " + event.player.getName());
            AbstractPlayerDamageModel damageModel = Objects.requireNonNull(event.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
            if (damageModel.hasTutorial)
                CapProvider.tutorialDone.add(event.player.getName());
            EntityPlayerMP playerMP = (EntityPlayerMP) event.player;
            FirstAid.NETWORKING.sendTo(new MessageConfiguration(damageModel, !playerMP.connection.netManager.isLocalChannel()), playerMP);
        }
    }

    @SubscribeEvent(priority =  EventPriority.LOW)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        hitList.remove(event.player);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote)
            world.getGameRules().setOrCreateGameRule("naturalRegeneration", Boolean.toString(FirstAidConfig.externalHealing.allowNaturalRegeneration));
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) //Mojang seems to wipe all caps on teleport
            FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(Objects.requireNonNull(event.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)), true), (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onPlayerBleedToDeath(PlayerKilledEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        if (damageModel != null) {
            damageModel.stopWaitingForHelp(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRevived(PlayerRevivedEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        if (damageModel != null) {
            damageModel.revivePlayer(player);
            damageModel.stopWaitingForHelp(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered() && !event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
            AbstractPlayerDamageModel damageModel = Objects.requireNonNull(event.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
            damageModel.runScaleLogic(event.player);
            damageModel.forEach(damageablePart -> damageablePart.heal(damageablePart.getMaxHealth(), event.player, false));
            damageModel.scheduleResync();
        }
    }
}
