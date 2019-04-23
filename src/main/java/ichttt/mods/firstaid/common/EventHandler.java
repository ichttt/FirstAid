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
import ichttt.mods.firstaid.common.damagesystem.distribution.PreferredDamageDistribution;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.ProjectileHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class EventHandler {
    public static final Random rand = new Random();
    @ObjectHolder("firstaid:debuff.heartbeat")
    public static final SoundEvent HEARTBEAT = FirstAidItems.getNull();
    @ObjectHolder("firstaid:morphine")
    public static final Potion MORPHINE = FirstAidItems.getNull();

    public static final Map<EntityPlayer, Pair<Entity, RayTraceResult>> hitList = new WeakHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !CommonUtils.hasDamageModel(entity))
            return;
        float amountToDamage = event.getAmount();
        EntityPlayer player = (EntityPlayer) entity;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        DamageSource source = event.getSource();

        if (amountToDamage == Float.MAX_VALUE) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof EntityPlayerMP)
                Arrays.stream(EnumPlayerPart.VALUES).forEach(part -> FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) player), new MessageReceiveDamage(part, Float.MAX_VALUE, 0F)));
            event.setCanceled(true);
            CommonUtils.killPlayer(player, source);
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
        if (result.type != RayTraceResult.Type.ENTITY)
            return;

        Entity entity = result.entity;
        if (!entity.world.isRemote && entity instanceof EntityPlayer) {
            hitList.put((EntityPlayer) entity, Pair.of(event.getEntity(), event.getRayTraceResult()));
        }
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (CommonUtils.hasDamageModel(obj)) {
            EntityPlayer player = (EntityPlayer) obj;
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
    public static void tickWorld(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (FirstAidConfig.SERVER.sleepHealPercentage.get() <= 0D) return;
        World world = event.world;
        if (!world.isRemote && world instanceof WorldServer && ((WorldServer) world).areAllPlayersAsleep()) {
            for (EntityPlayer player : world.playerEntities) {
                CommonUtils.getDamageModel(player).sleepHeal(player);
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!CommonUtils.hasDamageModel(entity))
            return;
        event.setCanceled(true);
        if (!FirstAidConfig.SERVER.allowOtherHealingItems.get())
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodStats.class.getName()))) {
            if (FirstAidConfig.SERVER.allowNaturalRegeneration.get())
                amount = amount * (float) (double) FirstAidConfig.SERVER.naturalRegenMultiplier.get();
        } else {
            amount = amount * (float) (double) FirstAidConfig.SERVER.otherRegenMultiplier.get();
        }
        HealthDistribution.distributeHealth(amount, (EntityPlayer) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            FirstAid.LOGGER.debug("Sending damage model to " + event.getPlayer().getName());
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(event.getPlayer());
            if (damageModel.hasTutorial)
                CapProvider.tutorialDone.add(event.getPlayer().getName().getString());
            EntityPlayerMP playerMP = (EntityPlayerMP) event.getPlayer();
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
            ((World) world).getGameRules().setOrCreateGameRule("naturalRegeneration", Boolean.toString(FirstAidConfig.SERVER.allowNaturalRegeneration.get()), ((World) world).getServer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getPlayer().world.isRemote && event.getPlayer() instanceof EntityPlayerMP) //Mojang seems to wipe all caps on teleport
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) event.getPlayer()), new MessageSyncDamageModel(CommonUtils.getDamageModel(event.getPlayer())));
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
}
