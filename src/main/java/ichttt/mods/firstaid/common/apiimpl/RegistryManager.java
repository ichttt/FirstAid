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

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

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
        List<Pair<EquipmentSlotType, EnumPlayerPart[]>> feetList = new ArrayList<>(2);
        feetList.add(Pair.of(EquipmentSlotType.FEET, new EnumPlayerPart[]{EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT}));
        feetList.add(Pair.of(EquipmentSlotType.LEGS, new EnumPlayerPart[]{EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG}));
        registry.bindDamageSourceStandard(DamageSource.FALL, feetList, false);
        registry.bindDamageSourceStandard(DamageSource.HOT_FLOOR, feetList, false);

        List<Pair<EquipmentSlotType, EnumPlayerPart[]>> headList = new ArrayList<>(1);
        headList.add(Pair.of(EquipmentSlotType.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        registry.bindDamageSourceStandard(DamageSource.ANVIL, headList, false);

        List<Pair<EquipmentSlotType, EnumPlayerPart[]>> headArmsList = new ArrayList<>(2);
        headArmsList.add(Pair.of(EquipmentSlotType.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        headArmsList.add(Pair.of(EquipmentSlotType.CHEST, new EnumPlayerPart[]{EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM}));
        registry.bindDamageSourceStandard(DamageSource.LIGHTNING_BOLT, headArmsList, true);

        registry.bindDamageSourceRandom(DamageSource.MAGIC, false, false);
        if (FirstAidConfig.hardMode) {
            List<Pair<EquipmentSlotType, EnumPlayerPart[]>> bodyList = new ArrayList<>(1);
            bodyList.add(Pair.of(EquipmentSlotType.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
            registry.bindDamageSourceStandard(DamageSource.STARVE, bodyList, false);

            List<Pair<EquipmentSlotType, EnumPlayerPart[]>> headBodyList = new ArrayList<>(2);
            headBodyList.add(Pair.of(EquipmentSlotType.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
            headBodyList.add(Pair.of(EquipmentSlotType.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
            registry.bindDamageSourceStandard(DamageSource.DROWN, headBodyList, true);
        } else {
            registry.bindDamageSourceRandom(DamageSource.STARVE, false, true);
            registry.bindDamageSourceRandom(DamageSource.DROWN, false, true);
        }
        registry.bindDamageSourceRandom(DamageSource.IN_WALL, false, true);

        //---DEBUFFS---
        DebuffBuilderFactory factory = DebuffBuilderFactory.getInstance();
        loadValuesFromConfig(factory, "blindness", () -> EventHandler.HEARTBEAT, FirstAidConfig.GENERAL.head.blindnessConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(factory, "nausea", null, FirstAidConfig.GENERAL.head.nauseaConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(factory, "nausea", null, FirstAidConfig.GENERAL.body.nauseaConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(factory, "weakness", FirstAidConfig.GENERAL.body.weaknessConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(factory, "mining_fatigue", FirstAidConfig.GENERAL.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);

        loadValuesFromConfig(factory, "slowness", FirstAidConfig.GENERAL.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, Supplier<SoundEvent> event, FirstAidConfig.General.ConditionOnHit config, EnumDebuffSlot slot) {
        float[] damageTaken = Floats.toArray(config.damageTaken.get());
        int[] debuffLength = Ints.toArray(config.debuffLength.get());
        if (debuffLength.length != damageTaken.length) {
            logError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (debuffLength.length == 0) {
            logError("The fields are empty!", potionName, slot);
            return;
        }

        float[] healthPercentageLeft = new float[damageTaken.length];
        System.arraycopy(damageTaken, 0, healthPercentageLeft, 0, damageTaken.length);
        Arrays.sort(healthPercentageLeft);
        ArrayUtils.reverse(healthPercentageLeft);
        if (!Arrays.equals(healthPercentageLeft, damageTaken)) {
            logError("The damageTaken field is not sorted right!", potionName, slot);
            return;
        }

        IDebuffBuilder builder = factory.newOnHitDebuffBuilder(potionName);
        builder.addEnableCondition(config.enabled::get);
        for (int i = 0; i < damageTaken.length; i++)
            builder.addBound(damageTaken[i], debuffLength[i]);

        if (event != null) builder.addSoundEffect(event);
        builder.register(slot);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, FirstAidConfig.General.ConditionConstant config, EnumDebuffSlot slot) {
        int[] debuffStrength = Ints.toArray(config.debuffStrength.get());
        float[] healthPercentageLeft = Floats.toArray(config.healthPercentageLeft.get());
        if (debuffStrength.length != healthPercentageLeft.length) {
            logError("The fields to not have the same amount of values!", potionName, slot);
            return;
        }
        if (healthPercentageLeft.length == 0) {
            logError("The fields are empty!", potionName, slot);
            return;
        }

        if (!ArrayUtils.isSorted(healthPercentageLeft)) {
            logError("The healthPercentageLeft field is not sorted right!", potionName, slot);
            return;
        }
        IDebuffBuilder builder = factory.newConstantDebuffBuilder(potionName);
        builder.addEnableCondition(config.enabled::get);
        for (int i = 0; i < healthPercentageLeft.length; i++)
            builder.addBound(healthPercentageLeft[i], debuffStrength[i]);

        builder.register(slot);
    }

    private static void logError(String error, String potionName, EnumDebuffSlot slot) {
        String errorMsg = String.format("Invalid config entry for debuff %s at part %s: %s", potionName, slot.toString(), error);
        FirstAid.LOGGER.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }

    public static void finalizeRegistries() {
        FirstAidRegistryImpl.finish();
        DebuffBuilderFactoryImpl.verify();
    }
}
