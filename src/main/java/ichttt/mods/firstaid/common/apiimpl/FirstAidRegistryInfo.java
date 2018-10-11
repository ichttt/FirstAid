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

package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.registry.DamageDistributionBuilder;
import ichttt.mods.firstaid.api.registry.DamageSourceEntry;
import ichttt.mods.firstaid.api.registry.DebuffEntry;
import ichttt.mods.firstaid.api.registry.HealingEntry;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class FirstAidRegistryInfo {
    public static final List<String> debuffConfigErrors = new ArrayList<>();
    public static void registerDebuffs(IForgeRegistry<DebuffEntry> registry) {
        DebuffBuilderFactory factory = DebuffBuilderFactory.getInstance();
        loadDebuff(registry, factory, "blindness", () -> FirstAidConfig.debuffs.head.blindness, EventHandler.HEARTBEAT, FirstAidConfig.debuffs.head.blindnessConditions, EnumDebuffSlot.HEAD);
        loadDebuff(registry, factory, "nausea", () -> FirstAidConfig.debuffs.head.nausea, null, FirstAidConfig.debuffs.head.nauseaConditions, EnumDebuffSlot.HEAD);
        loadDebuff(registry, factory, "nausea", () -> FirstAidConfig.debuffs.body.nausea, null, FirstAidConfig.debuffs.body.nauseaConditions, EnumDebuffSlot.BODY);
        loadDebuff(registry, factory, "weakness", () -> FirstAidConfig.debuffs.body.weakness, FirstAidConfig.debuffs.body.weaknessConditions, EnumDebuffSlot.BODY);
        loadDebuff(registry, factory, "mining_fatigue", () -> FirstAidConfig.debuffs.arms.mining_fatigue, FirstAidConfig.debuffs.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);
        loadDebuff(registry, factory, "slowness", () -> FirstAidConfig.debuffs.legsAndFeet.slowness, FirstAidConfig.debuffs.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
    }

    public static void registerHealing(IForgeRegistry<HealingEntry> registry) {
        loadHealing(registry, "bandage", stack -> new PartHealer(18 * 20, 4, stack), 2500);
        loadHealing(registry, "plaster", stack -> new PartHealer(22 * 20, 2, stack), 3000);
    }

    public static void registerDamageSources(IForgeRegistry<DamageSourceEntry> registry) {
        DamageDistributionBuilder builder = Objects.requireNonNull(DamageDistributionBuilder.getImpl());
        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> feetList = new ArrayList<>(2);
        feetList.add(Pair.of(EntityEquipmentSlot.FEET, new EnumPlayerPart[]{EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT}));
        feetList.add(Pair.of(EntityEquipmentSlot.LEGS, new EnumPlayerPart[]{EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG}));
        registerDamageSource(registry, "fall", builder.standardDist(feetList));

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headList = new ArrayList<>(1);
        headList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        registerDamageSource(registry, "anvil", builder.standardDist(headList));

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> bodyList = new ArrayList<>(1);
        bodyList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
        registerDamageSource(registry, "starve", builder.standardDist(bodyList));

        registerDamageSource(registry, "magic", builder.randomDist(false, false));
        registerDamageSource(registry,"drown", builder.randomDist(false, true));
        registerDamageSource(registry,"inWall", builder.randomDist(false, true));
    }

    private static void loadDebuff(IForgeRegistry<DebuffEntry> registry, DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, SoundEvent event, FirstAidConfig.Debuffs.ConditionOnHit config, EnumDebuffSlot slot) {
        if (config.debuffLength.length != config.damageTaken.length) {
            logDebuffError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (config.debuffLength.length == 0) {
            logDebuffError("The fields are empty!", potionName, slot);
            return;
        }

        float[] healthPercentageLeft = new float[config.damageTaken.length];
        System.arraycopy(config.damageTaken, 0, healthPercentageLeft, 0, config.damageTaken.length);
        Arrays.sort(healthPercentageLeft);
        ArrayUtils.reverse(healthPercentageLeft);
        if (!Arrays.equals(healthPercentageLeft, config.damageTaken)) {
            logDebuffError("The damageTaken field is not sorted right!", potionName, slot);
            return;
        }

        IDebuffBuilder builder = factory.newOnHitDebuffBuilder(potionName);
        builder.addEnableCondition(enableCondition);
        for (int i = 0; i < config.damageTaken.length; i++)
            builder.addBound(config.damageTaken[i], config.debuffLength[i]);

        if (event != null) builder.addSoundEffect(event);
        DebuffEntry entry = builder.build(slot);
        entry.setRegistryName(new ResourceLocation(FirstAid.MODID, slot.toString().toLowerCase(Locale.ROOT) + "_" + potionName));
        registry.register(entry);
    }

    private static void loadDebuff(IForgeRegistry<DebuffEntry> debuffEntries, DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, FirstAidConfig.Debuffs.ConditionConstant config, EnumDebuffSlot slot) {
        if (config.debuffStrength.length != config.healthPercentageLeft.length) {
            logDebuffError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (config.healthPercentageLeft.length == 0) {
            logDebuffError("The fields are empty!", potionName, slot);
            return;
        }

        if (!ArrayUtils.isSorted(config.healthPercentageLeft)) {
            logDebuffError("The healthPercentageLeft field is not sorted right!", potionName, slot);
            return;
        }
        IDebuffBuilder builder = factory.newConstantDebuffBuilder(potionName);
        builder.addEnableCondition(enableCondition);
        for (int i = 0; i < config.healthPercentageLeft.length; i++)
            builder.addBound(config.healthPercentageLeft[i], config.debuffStrength[i]);

        DebuffEntry entry = builder.build(slot);
        entry.setRegistryName(new ResourceLocation(FirstAid.MODID, slot.toString().toLowerCase(Locale.ROOT) + "_" + potionName));
        debuffEntries.register(entry);
    }

    private static void logDebuffError(String error, String potionName, EnumDebuffSlot slot) {
        String errorMsg = String.format("Invalid config entry for debuff %s at part %s: %s", potionName, slot.toString(), error);
        FirstAid.LOGGER.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }

    private static void registerDamageSource(IForgeRegistry<DamageSourceEntry> registry, String damageType, IDamageDistribution distribution) {
        DamageSourceEntry entry = new DamageSourceEntry(damageType, distribution);
        entry.setRegistryName(new ResourceLocation(FirstAid.MODID, damageType));
        registry.register(entry);
    }

    private static void loadHealing(IForgeRegistry<HealingEntry> registry, String itemName, Function<ItemStack, AbstractPartHealer> function, int applyTime) {
        ResourceLocation location = new ResourceLocation(FirstAid.MODID, itemName);
        Item item = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(location));
        HealingEntry entry = new HealingEntry(item, function, applyTime);
        entry.setRegistryName(location);
        registry.register(entry);
    }
}
