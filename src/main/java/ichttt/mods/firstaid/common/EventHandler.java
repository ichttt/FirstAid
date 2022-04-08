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

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.PlayerSizeHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.food.FoodData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmlserverevents.FMLServerStoppedEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class EventHandler {
    public static final Random rand = new Random();
    @ObjectHolder("firstaid:debuff.heartbeat")
    public static final SoundEvent HEARTBEAT = FirstAidItems.getNull();
    @ObjectHolder("firstaid:morphine")
    public static final MobEffect MORPHINE = FirstAidItems.getNull();
    @ObjectHolder("minecraft:resistance")
    public static final Effect DAMAGE_RESISTANCE = FirstAidItems.getNull();

    public static final Map<Player, Pair<Entity, HitResult>> hitList = new WeakHashMap<>();
    private static final Field LOOT_ENTRIES_FIELD = ObfuscationReflectionHelper.findField(LootPool.class, "f_79023_");

    @SubscribeEvent(priority = EventPriority.LOWEST) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide || !CommonUtils.hasDamageModel(entity))
            return;
        float amountToDamage = event.getAmount();
        Player player = (Player) entity;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        DamageSource source = event.getSource();

        if (amountToDamage == Float.MAX_VALUE) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof ServerPlayer)
                FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageSyncDamageModel(damageModel, false));
            event.setCanceled(true);
            CommonUtils.killPlayer(damageModel, player, source);
            return;
        }

        boolean addStat = amountToDamage < 3.4028235E37F;
        IDamageDistribution damageDistribution = FirstAidRegistryImpl.INSTANCE.getDamageDistributionForSource(source);

        if (source.isProjectile()) {
            Pair<Entity, HitResult> rayTraceResult = hitList.remove(player);
            if (rayTraceResult != null) {
                Entity entityProjectile = rayTraceResult.getLeft();
                EquipmentSlot slot = PlayerSizeHelper.getSlotTypeForProjectileHit(entityProjectile, player);
                if (slot != null) {
                    EnumPlayerPart[] possibleParts = CommonUtils.getPartArrayForSlot(slot);
                    damageDistribution = new StandardDamageDistribution(Collections.singletonList(Pair.of(slot, possibleParts)), false, true);
                }
            }
        }
        if (damageDistribution == null) {
            // No given distribution found, and no projectile distribution either. Let's check if we can tell by the source where we should apply the damage, otherwise fall back to random
            damageDistribution = PlayerSizeHelper.getMeleeDistribution(player, source);
            if (damageDistribution == null) {
                damageDistribution = RandomDamageDistribution.getDefault();
            }
        }

        DamageDistribution.handleDamageTaken(damageDistribution, damageModel, amountToDamage, player, source, addStat, true);

        event.setCanceled(true);

        hitList.remove(player);
    }

    @SubscribeEvent(priority =  EventPriority.LOWEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        HitResult result = event.getRayTraceResult();
        if (result.getType() != HitResult.Type.ENTITY)
            return;

        Entity entity = ((EntityHitResult) result).getEntity();
        if (!entity.level.isClientSide && entity instanceof Player) {
            hitList.put((Player) entity, Pair.of(event.getEntity(), event.getRayTraceResult()));
        }
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (CommonUtils.hasDamageModel(obj)) {
            Player player = (Player) obj;
            AbstractPlayerDamageModel damageModel = PlayerDamageModel.create();
            event.addCapability(CapProvider.IDENTIFIER, new CapProvider(damageModel));
            //replace the data manager with our wrapper to grab absorption
            player.entityData = new DataManagerWrapper(player, player.entityData);
        }
    }

    @SubscribeEvent
    public static void tickPlayers(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.getAbilities().invulnerable) {
            if (!event.player.isAlive()) return;
            CommonUtils.getDamageModel(event.player).tick(event.player.level, event.player);
            hitList.remove(event.player); //Damage should be done in the same tick as the hit was noted, otherwise we got a false-positive
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (ModList.get().isLoaded("morpheus")) return;
        for (Player player : event.getWorld().players()) {
            if (player.isSleepingLongEnough())
                CommonUtils.getDamageModel(player).sleepHeal(player);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableName = event.getName();
        LootPool pool = null;
        int bandage = 0, plaster = 0, morphine = 0;
        if (tableName.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) {
            pool = event.getTable().getPool("main");
            bandage = 8;
            plaster = 16;
            morphine = 4;
        } else if (tableName.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR) || tableName.equals(BuiltInLootTables.STRONGHOLD_CROSSING) || tableName.equals(BuiltInLootTables.ABANDONED_MINESHAFT)) {
            pool = event.getTable().getPool("main");
            bandage = 20;
            plaster = 24;
            morphine = 8;
        } else if (tableName.equals(BuiltInLootTables.VILLAGE_BUTCHER)) {
            pool = event.getTable().getPool("main");
            bandage = 4;
            plaster = 16;
            morphine = 2;
        } else if (tableName.equals(BuiltInLootTables.IGLOO_CHEST)) {
            pool = event.getTable().getPool("main");
            bandage = 4;
            plaster = 8;
            morphine = 2;
        }

        if (pool != null) {
            List<LootPoolEntryContainer> lootEntries;
            try {
                lootEntries = (List<LootPoolEntryContainer>) LOOT_ENTRIES_FIELD.get(pool);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Reflection failed!", e);
            }
            lootEntries.add(LootItem.lootTableItem(() -> FirstAidItems.BANDAGE)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                    .setWeight(bandage)
                    .setQuality(0)
                    .build());
            lootEntries.add(LootItem.lootTableItem(() -> FirstAidItems.PLASTER)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5)))
                    .setWeight(plaster)
                    .setQuality(0)
                    .build());
            lootEntries.add(LootItem.lootTableItem(() -> FirstAidItems.MORPHINE)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                    .setWeight(morphine)
                    .setQuality(0)
                    .build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!CommonUtils.hasDamageModel(entity))
            return;
        event.setCanceled(true);
        if (entity.level.isClientSide || !FirstAidConfig.SERVER.allowOtherHealingItems.get())
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodData.class.getName()))) {
            if (FirstAidConfig.SERVER.allowNaturalRegeneration.get())
                amount = amount * (float) (double) FirstAidConfig.SERVER.naturalRegenMultiplier.get();
        } else {
            amount = amount * (float) (double) FirstAidConfig.SERVER.otherRegenMultiplier.get();
        }
        if (FirstAidConfig.GENERAL.debug.get()) {
            CommonUtils.debugLogStacktrace("External healing: : " + amount);
        }
        HealthDistribution.distributeHealth(amount, (Player) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            FirstAid.LOGGER.debug("Sending damage model to " + event.getPlayer().getName());
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(event.getPlayer());
            if (damageModel.hasTutorial)
                CapProvider.tutorialDone.add(event.getPlayer().getName().getString());
            ServerPlayer playerMP = (ServerPlayer) event.getPlayer();
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> playerMP), new MessageConfiguration(damageModel.serializeNBT()));
        }
    }

    @SubscribeEvent(priority =  EventPriority.LOW)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        hitList.remove(event.getPlayer());
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        LevelAccessor world = event.getWorld();
        if (!world.isClientSide() && world instanceof Level)
            ((Level) world).getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(FirstAidConfig.SERVER.allowNaturalRegeneration.get(), ((Level) world).getServer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();
        if (!player.level.isClientSide && player instanceof ServerPlayer) //Mojang seems to wipe all caps on teleport
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageSyncDamageModel(CommonUtils.getDamageModel(player), true));
    }

    @SubscribeEvent
    public static void onServerStop(FMLServerStoppedEvent event) {
        FirstAid.LOGGER.debug("Cleaning up");
        CapProvider.tutorialDone.clear();
        EventHandler.hitList.clear();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        DebugDamageCommand.register(event.getDispatcher());
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
        Player player = event.getPlayer();
        if (!event.isEndConquered() && !player.level.isClientSide && player instanceof ServerPlayer) {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            damageModel.runScaleLogic(player);
            damageModel.forEach(damageablePart -> damageablePart.heal(damageablePart.getMaxHealth(), player, false));
            damageModel.scheduleResync();
        }
    }
}
