package de.technikforlife.firstaid;

import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapWrapper;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import de.technikforlife.firstaid.damagesystem.distribution.DamageDistribution;
import de.technikforlife.firstaid.damagesystem.distribution.HealthDistribution;
import de.technikforlife.firstaid.items.FirstAidItems;
import de.technikforlife.firstaid.network.MessageReceiveDamageModel;
import de.technikforlife.firstaid.util.ArmorUtils;
import de.technikforlife.firstaid.util.DataManagerWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.Random;

import static de.technikforlife.firstaid.damagesystem.distribution.DamageDistributions.*;

public class EventHandler {
    public static final Random rand = new Random();

    @SubscribeEvent(priority = EventPriority.LOW) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        float amountToDamage = event.getAmount();
        if (entity.getEntityWorld().isRemote || !entity.hasCapability(CapabilityExtendedHealthSystem.INSTANCE, null))
            return;
        if (amountToDamage == Float.MAX_VALUE) {
            event.setCanceled(true);
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        DamageSource source = event.getSource();
        String sourceType = source.damageType;
        DamageDistribution damageDistribution;
        switch (sourceType) {
            case "fall":
            case "hotFloor":
                damageDistribution = FALL_DMG;
                break;
            case "fallingBlock":
            case "anvil":
                damageDistribution = HEAD;
                break;
            case "starve":
                damageDistribution = STARVE;
                break;
            case "magic":
            case "drown":
                damageDistribution = FULL_RANDOM_DIST;
                break;
            default:
                damageDistribution = SEMI_RANDOM_DIST;
        }
        float origAmount =amountToDamage;
        amountToDamage = ArmorUtils.applyGlobalPotionModifieres(player, source, amountToDamage);

        //VANILLA COPY - combat tracker and exhaustion
        if (amountToDamage != 0.0F) {
            player.addExhaustion(source.getHungerDamage());
            //2nd param is actually never queried, no need to worry about wrong values
            player.getCombatTracker().trackDamage(source, -1, amountToDamage);
        }

        boolean addStat = amountToDamage < 3.4028235E37F;
        float left = damageDistribution.distributeDamage(amountToDamage, player, source, addStat);
        if (left > 0) {
            damageDistribution = SEMI_RANDOM_DIST;
            damageDistribution.distributeDamage(left, player, source, addStat);
        }

        event.setCanceled(true);
        if (damageModel.isDead() && (!FirstAidConfig.allowOtherHealingItems || !player.checkTotemDeathProtection(source)))
            player.setHealth(0F);
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer)) {
            EntityPlayer player = (EntityPlayer) obj;
            event.addCapability(CapWrapper.IDENTIFIER, new CapWrapper(player));
            //replace the data manager with our wrapper to grab absorption
            player.dataManager = new DataManagerWrapper(player, player.dataManager);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.isCreative())
            PlayerDataManager.tickPlayer(event.player);
    }

    @SubscribeEvent
    public static void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.crafting;
        if (stack.getItem() == FirstAidItems.BANDAGE) {
            String username = event.player.getName();
            if (username.equalsIgnoreCase("ichun"))
                stack.setStackDisplayName("MediChun's Healthkit"); //Yup, I *had* to do this
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
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving instanceof EntityPlayer && !(entityLiving instanceof FakePlayer)) {
            PlayerDataManager.clearPlayer((EntityPlayer) entityLiving);
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(FirstAid.MODID)) {
            ConfigManager.sync(FirstAid.MODID, Config.Type.INSTANCE);
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.hasCapability(CapabilityExtendedHealthSystem.INSTANCE, null))
            return;
        event.setCanceled(true);
        if (!FirstAidConfig.allowOtherHealingItems)
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (FirstAidConfig.allowNaturalRegeneration && Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodStats.class.getName())))
            amount = amount * 0.75F;
        HealthDistribution.distributeHealth(amount, (EntityPlayer) entity);
        FirstAid.proxy.healClient(amount);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            FirstAid.logger.debug("Sending damage model to " + event.player.getDisplayNameString());
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamageModel(PlayerDataManager.getDamageModel(event.player)), (EntityPlayerMP) event.player);
        }
    }
}
