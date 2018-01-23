package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.CapProvider;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.network.MessageReceiveConfiguration;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.network.MessageResync;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.Random;

public class EventHandler {
    public static final Random rand = new Random();
    public static final SoundEvent HEARTBEAT = new SoundEvent(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat")).setRegistryName(new ResourceLocation(FirstAid.MODID, "debuff.heartbeat"));

    @SubscribeEvent(priority = EventPriority.LOWEST) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        float amountToDamage = event.getAmount();
        if (entity.getEntityWorld().isRemote || !(entity instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) entity;
        AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        DamageSource source = event.getSource();

        if (amountToDamage == Float.MAX_VALUE) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof EntityPlayerMP)
                Arrays.stream(EnumPlayerPart.VALUES).forEach(part -> FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part, Float.MAX_VALUE, 0F), (EntityPlayerMP) player));
            event.setCanceled(true);
            CommonUtils.killPlayer(player, source);
            return;
        }

        amountToDamage = ArmorUtils.applyGlobalPotionModifiers(player, source, amountToDamage);

        //VANILLA COPY - combat tracker and exhaustion
        if (amountToDamage != 0.0F) {
            player.addExhaustion(source.getHungerDamage());
            //2nd param is actually never queried, no need to worry about wrong values
            player.getCombatTracker().trackDamage(source, -1, amountToDamage);
        }

        boolean addStat = amountToDamage < 3.4028235E37F;
        IDamageDistribution damageDistribution = FirstAidRegistryImpl.INSTANCE.getDamageDistribution(source);

        float left = damageDistribution.distributeDamage(amountToDamage, player, source, addStat);
        if (left > 0) {
            damageDistribution = RandomDamageDistribution.NEAREST_KILL;
            damageDistribution.distributeDamage(left, player, source, addStat);
        }

        event.setCanceled(true);
        if (damageModel.isDead(player))
            CommonUtils.killPlayer(player, source);
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer)) {
            EntityPlayer player = (EntityPlayer) obj;
            AbstractPlayerDamageModel damageModel;
            if (player.world.isRemote)
                damageModel = FirstAid.activeDamageConfig == null ? PlayerDamageModel.createTemp() : PlayerDamageModel.create();
            else {
                FirstAid.activeDamageConfig = FirstAidConfig.damageSystem;
                FirstAid.activeHealingConfig = FirstAidConfig.externalHealing;
                FirstAid.scaleMaxHealth = FirstAidConfig.scaleMaxHealth;
                damageModel = PlayerDamageModel.create();
            }
            event.addCapability(CapProvider.IDENTIFIER, new CapProvider(player, damageModel));
            //replace the data manager with our wrapper to grab absorption
            player.dataManager = new DataManagerWrapper(player, player.dataManager);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.isCreative()) {
            PlayerDataManager.tickPlayer(event.player);
        }
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

    @SubscribeEvent(priority = EventPriority.LOW)
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
        if (!FirstAid.activeHealingConfig.allowOtherHealingItems)
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (FirstAid.activeHealingConfig.allowNaturalRegeneration && Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodStats.class.getName())))
            amount = amount * (float) FirstAid.activeHealingConfig.naturalRegenMultiplier;
        else
            amount = amount * (float) FirstAid.activeHealingConfig.otherRegenMultiplier;
        HealthDistribution.distributeHealth(amount, (EntityPlayer) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            FirstAid.logger.debug("Sending damage model to " + event.player.getName());
            AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(event.player);
            if (damageModel.hasTutorial)
                PlayerDataManager.tutorialDone.add(event.player.getName());
            FirstAid.NETWORKING.sendTo(new MessageReceiveConfiguration(damageModel, FirstAidConfig.externalHealing, FirstAidConfig.damageSystem, FirstAidConfig.scaleMaxHealth), (EntityPlayerMP) event.player);
        }
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
            FirstAid.NETWORKING.sendTo(new MessageResync(PlayerDataManager.getDamageModel(event.player)), (EntityPlayerMP) event.player);
    }
}
