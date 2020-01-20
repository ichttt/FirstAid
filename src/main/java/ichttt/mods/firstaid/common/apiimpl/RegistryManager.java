/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumPlayerDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class RegistryManager {
    public static final List<String> debuffConfigErrors = new ArrayList<>();

    public static void setupRegistries() {
        FirstAidRegistry.setImpl(FirstAidRegistryImpl.INSTANCE);
        DebuffBuilderFactory.setInstance(DebuffBuilderFactoryImpl.INSTANCE);
    }

    public static void registerDefaults() {
        FirstAid.LOGGER.debug("Registering defaults registry values");
        FirstAidRegistry registry = Objects.requireNonNull(FirstAidRegistry.getImpl());

        //---DAMAGE SOURCES---
        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> feetList = new ArrayList<>(2);
        registry.bindDamageSourceStandard(DamageSource.FALL, false, EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS);
        registry.bindDamageSourceStandard(DamageSource.HOT_FLOOR, false, EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headList = new ArrayList<>(1);
        headList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        registry.bindDamageSourceStandard(DamageSource.ANVIL, false, EntityEquipmentSlot.HEAD);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headArmsList = new ArrayList<>(2);
        headArmsList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        headArmsList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM}));
        registry.bindDamageSourceStandard(DamageSource.LIGHTNING_BOLT, true, headArmsList, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST);

        registry.bindDamageSourceRandom(DamageSource.MAGIC, false, false);
        if (FirstAidConfig.hardMode) {
            List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> bodyList = new ArrayList<>(1);
            bodyList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
            registry.bindDamageSourceStandard(DamageSource.STARVE, false, bodyList, EntityEquipmentSlot.CHEST);

            List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headBodyList = new ArrayList<>(2);
            headBodyList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
            headBodyList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
            registry.bindDamageSourceStandard(DamageSource.DROWN, true, headBodyList, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST);
        } else {
            registry.bindDamageSourceRandom(DamageSource.STARVE, false, true);
            registry.bindDamageSourceRandom(DamageSource.DROWN, false, true);
        }
        registry.bindDamageSourceRandom(DamageSource.IN_WALL, false, true);

        //---DEBUFFS---
        DebuffBuilderFactory factory = DebuffBuilderFactory.getInstance();
        loadValuesFromConfig(factory, "blindness", () -> FirstAidConfig.debuffs.head.blindness, EventHandler.HEARTBEAT, FirstAidConfig.debuffs.head.blindnessConditions, EnumPlayerDebuffSlot.HEAD, EntityEquipmentSlot.HEAD);
        loadValuesFromConfig(factory, "nausea", () -> FirstAidConfig.debuffs.head.nausea, null, FirstAidConfig.debuffs.head.nauseaConditions, EnumPlayerDebuffSlot.HEAD, EntityEquipmentSlot.HEAD);
        loadValuesFromConfig(factory, "nausea", () -> FirstAidConfig.debuffs.body.nausea, null, FirstAidConfig.debuffs.body.nauseaConditions, EnumPlayerDebuffSlot.BODY, EntityEquipmentSlot.CHEST);
        loadValuesFromConfig(factory, "weakness", () -> FirstAidConfig.debuffs.body.weakness, FirstAidConfig.debuffs.body.weaknessConditions, EnumPlayerDebuffSlot.BODY, EntityEquipmentSlot.CHEST);
        loadValuesFromConfig(factory, "mining_fatigue", () -> FirstAidConfig.debuffs.arms.mining_fatigue, FirstAidConfig.debuffs.arms.miningFatigueConditions, EnumPlayerDebuffSlot.ARMS, EntityEquipmentSlot.CHEST);
        loadValuesFromConfig(factory, "slowness", () -> FirstAidConfig.debuffs.legsAndFeet.slowness, FirstAidConfig.debuffs.legsAndFeet.slownessConditions, EnumPlayerDebuffSlot.LEGS_AND_FEET, EntityEquipmentSlot.LEGS);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, SoundEvent event, FirstAidConfig.Debuffs.ConditionOnHit config, EnumPlayerDebuffSlot slot, EntityEquipmentSlot generalSlot) {
        if (config.debuffLength.length != config.damageTaken.length) {
            logError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (config.debuffLength.length == 0) {
            logError("The fields are empty!", potionName, slot);
            return;
        }

        float[] healthPercentageLeft = new float[config.damageTaken.length];
        System.arraycopy(config.damageTaken, 0, healthPercentageLeft, 0, config.damageTaken.length);
        Arrays.sort(healthPercentageLeft);
        ArrayUtils.reverse(healthPercentageLeft);
        if (!Arrays.equals(healthPercentageLeft, config.damageTaken)) {
            logError("The damageTaken field is not sorted right!", potionName, slot);
            return;
        }

        IDebuffBuilder builder = factory.newOnHitDebuffBuilder(potionName);
        builder.addEnableCondition(enableCondition);
        for (int i = 0; i < config.damageTaken.length; i++)
            builder.addBound(config.damageTaken[i], config.debuffLength[i]);

        if (event != null) builder.addSoundEffect(event);
        builder.register(slot, generalSlot);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, FirstAidConfig.Debuffs.ConditionConstant config, EnumPlayerDebuffSlot slot, EntityEquipmentSlot generalSlot) {
        if (config.debuffStrength.length != config.healthPercentageLeft.length) {
            logError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (config.healthPercentageLeft.length == 0) {
            logError("The fields are empty!", potionName, slot);
            return;
        }

        if (!ArrayUtils.isSorted(config.healthPercentageLeft)) {
            logError("The healthPercentageLeft field is not sorted right!", potionName, slot);
            return;
        }
        IDebuffBuilder builder = factory.newConstantDebuffBuilder(potionName);
        builder.addEnableCondition(enableCondition);
        for (int i = 0; i < config.healthPercentageLeft.length; i++)
            builder.addBound(config.healthPercentageLeft[i], config.debuffStrength[i]);

        builder.register(slot, generalSlot);
    }

    private static void logError(String error, String potionName, EnumPlayerDebuffSlot slot) {
        String errorMsg = String.format("Invalid config entry for debuff %s at part %s: %s", potionName, slot.toString(), error);
        FirstAid.LOGGER.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }

    public static void finalizeRegistries() {
        FirstAidRegistryImpl.finish();
        DebuffBuilderFactoryImpl.verify();
    }
}
