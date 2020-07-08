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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.PreferredDamageDistribution;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.ProjectileHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class EventHandler {
    public static final Random rand = new Random();
    @ObjectHolder("firstaid:debuff.heartbeat")
    public static final SoundEvent HEARTBEAT = FirstAidItems.getNull();
    @ObjectHolder("firstaid:morphine")
    public static final Effect MORPHINE = FirstAidItems.getNull();

    public static final Map<PlayerEntity, Pair<Entity, RayTraceResult>> hitList = new WeakHashMap<>();
    private static final Field LOOT_ENTRIES_FIELD = ObfuscationReflectionHelper.findField(LootPool.class, "field_186453_a");

    @SubscribeEvent(priority = EventPriority.LOWEST) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.world.isRemote || !CommonUtils.hasDamageModel(entity))
            return;
        float amountToDamage = event.getAmount();
        PlayerEntity player = (PlayerEntity) entity;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        DamageSource source = event.getSource();

        if (amountToDamage == Float.MAX_VALUE) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof ServerPlayerEntity)
                FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageSyncDamageModel(damageModel, false));
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
                EquipmentSlotType slot = ProjectileHelper.getPartByPosition(entityProjectile, player);
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
        if (result.getType() != RayTraceResult.Type.ENTITY)
            return;

        Entity entity = ((EntityRayTraceResult) result).getEntity();
        if (!entity.world.isRemote && entity instanceof PlayerEntity) {
            hitList.put((PlayerEntity) entity, Pair.of(event.getEntity(), event.getRayTraceResult()));
        }
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (CommonUtils.hasDamageModel(obj)) {
            PlayerEntity player = (PlayerEntity) obj;
            AbstractPlayerDamageModel damageModel = PlayerDamageModel.create();
            event.addCapability(CapProvider.IDENTIFIER, new CapProvider(damageModel));
            //replace the data manager with our wrapper to grab absorption
            player.dataManager = new DataManagerWrapper(player, player.dataManager);
        }
    }

    @SubscribeEvent
    public static void tickPlayers(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && CommonUtils.isSurvivalOrAdventure(event.player)) {
            if (!event.player.isAlive()) return;
            CommonUtils.getDamageModel(event.player).tick(event.player.world, event.player);
            hitList.remove(event.player); //Damage should be done in the same tick as the hit was noted, otherwise we got a false-positive
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (ModList.get().isLoaded("morpheus")) return;
        for (PlayerEntity player : event.getWorld().getPlayers()) {
            if (player.isPlayerFullyAsleep())
                CommonUtils.getDamageModel(player).sleepHeal(player);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableName = event.getName();
        LootPool pool = null;
        int bandage = 0, plaster = 0, morphine = 0;
        if (tableName.equals(LootTables.CHESTS_SPAWN_BONUS_CHEST)) {
            pool = event.getTable().getPool("main");
            bandage = 8;
            plaster = 16;
            morphine = 4;
        } else if (tableName.equals(LootTables.CHESTS_STRONGHOLD_CORRIDOR) || tableName.equals(LootTables.CHESTS_STRONGHOLD_CROSSING) || tableName.equals(LootTables.CHESTS_ABANDONED_MINESHAFT)) {
            pool = event.getTable().getPool("main");
            bandage = 20;
            plaster = 24;
            morphine = 8;
        } else if (tableName.equals(LootTables.CHESTS_VILLAGE_VILLAGE_BUTCHER)) {
            pool = event.getTable().getPool("main");
            bandage = 4;
            plaster = 16;
            morphine = 2;
        } else if (tableName.equals(LootTables.CHESTS_IGLOO_CHEST)) {
            pool = event.getTable().getPool("main");
            bandage = 4;
            plaster = 8;
            morphine = 2;
        }

        if (pool != null) {
            List<LootEntry> lootEntries;
            try {
                lootEntries = (List<LootEntry>) LOOT_ENTRIES_FIELD.get(pool);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Reflection failed!", e);
            }
            lootEntries.add(ItemLootEntry.builder(() -> FirstAidItems.BANDAGE)
                    .acceptFunction(SetCount.builder(new RandomValueRange(1, 3)))
                    .weight(bandage)
                    .quality(0)
                    .build());
            lootEntries.add(ItemLootEntry.builder(() -> FirstAidItems.PLASTER)
                    .acceptFunction(SetCount.builder(new RandomValueRange(1, 5)))
                    .weight(plaster)
                    .quality(0)
                    .build());
            lootEntries.add(ItemLootEntry.builder(() -> FirstAidItems.MORPHINE)
                    .acceptFunction(SetCount.builder(new RandomValueRange(1, 2)))
                    .weight(morphine)
                    .quality(0)
                    .build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!CommonUtils.hasDamageModel(entity))
            return;
        event.setCanceled(true);
        if (entity.world.isRemote || !FirstAidConfig.SERVER.allowOtherHealingItems.get())
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodStats.class.getName()))) {
            if (FirstAidConfig.SERVER.allowNaturalRegeneration.get())
                amount = amount * (float) (double) FirstAidConfig.SERVER.naturalRegenMultiplier.get();
        } else {
            amount = amount * (float) (double) FirstAidConfig.SERVER.otherRegenMultiplier.get();
        }
        if (FirstAidConfig.GENERAL.debug.get()) {
            CommonUtils.debugLogStacktrace("External healing: : " + amount);
        }
        HealthDistribution.distributeHealth(amount, (PlayerEntity) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            FirstAid.LOGGER.debug("Sending damage model to " + event.getPlayer().getName());
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(event.getPlayer());
            if (damageModel.hasTutorial)
                CapProvider.tutorialDone.add(event.getPlayer().getName().getString());
            ServerPlayerEntity playerMP = (ServerPlayerEntity) event.getPlayer();
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> playerMP), new MessageConfiguration(damageModel.serializeNBT()));
        }
    }

    @SubscribeEvent(priority =  EventPriority.LOW)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        hitList.remove(event.getPlayer());
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        if (!world.isRemote() && world instanceof World)
            ((World) world).getGameRules().get(GameRules.NATURAL_REGENERATION).set(FirstAidConfig.SERVER.allowNaturalRegeneration.get(), ((World) world).getServer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.world.isRemote && player instanceof ServerPlayerEntity) //Mojang seems to wipe all caps on teleport
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageSyncDamageModel(CommonUtils.getDamageModel(player), true));
    }

    @SubscribeEvent
    public static void beforeServerStart(FMLServerStartingEvent event) {
        DebugDamageCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onServerStop(FMLServerStoppedEvent event) {
        FirstAid.LOGGER.debug("Cleaning up");
        CapProvider.tutorialDone.clear();
        EventHandler.hitList.clear();
    }
//
//    @SubscribeEvent TODO PR comapt
//    public static void onPlayerBleedToDeath(PlayerKilledEvent event) {
//        EntityPlayer player = event.getEntityPlayer();
//        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
//        if (damageModel != null) {
//            damageModel.stopWaitingForHelp(player);
//        }
//    }
//
//    @SubscribeEvent
//    public static void onPlayerRevived(PlayerRevivedEvent event) {
//        EntityPlayer player = event.getEntityPlayer();
//        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
//        if (damageModel != null) {
//            damageModel.revivePlayer(player);
//            damageModel.stopWaitingForHelp(player);
//        }
//    }
//


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!event.isEndConquered() && !player.world.isRemote && player instanceof ServerPlayerEntity) {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            damageModel.runScaleLogic(player);
            damageModel.forEach(damageablePart -> damageablePart.heal(damageablePart.getMaxHealth(), player, false));
            damageModel.scheduleResync();
        }
    }
}
