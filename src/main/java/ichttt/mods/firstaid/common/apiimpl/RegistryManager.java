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

package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.DamageDistributionBuilderFactory;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.distribution.DamageDistributionBuilderFactoryImpl;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.apache.commons.lang3.ArrayUtils;

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
        DamageDistributionBuilderFactory.setInstance(DamageDistributionBuilderFactoryImpl.INSTANCE);
    }

    public static void registerDefaults() {
        FirstAid.LOGGER.debug("Registering defaults registry values");
        DamageDistributionBuilderFactory distributionBuilderFactory = Objects.requireNonNull(DamageDistributionBuilderFactory.getInstance());

        //---DAMAGE SOURCES---
        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EntityEquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)
                .addDistributionLayer(EntityEquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)
                .registerStatic(DamageSource.FALL, DamageSource.HOT_FLOOR);

        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                .registerStatic(DamageSource.ANVIL);

        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                .addDistributionLayer(EntityEquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM)
                .ignoreOrder()
                .registerStatic(DamageSource.LIGHTNING_BOLT);

        distributionBuilderFactory.newRandomBuilder().registerStatic(DamageSource.MAGIC);

        if (FirstAidConfig.hardMode) {
            distributionBuilderFactory.newStandardBuilder()
                    .addDistributionLayer(EntityEquipmentSlot.CHEST, EnumPlayerPart.BODY)
                    .registerStatic(DamageSource.STARVE);

            distributionBuilderFactory.newStandardBuilder()
                    .addDistributionLayer(EntityEquipmentSlot.CHEST, EnumPlayerPart.BODY)
                    .addDistributionLayer(EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                    .ignoreOrder()
                    .registerStatic(DamageSource.DROWN);
        } else {
            distributionBuilderFactory.newRandomBuilder().tryNoKill().registerStatic(DamageSource.STARVE, DamageSource.DROWN);
        }
        distributionBuilderFactory.newRandomBuilder().tryNoKill().registerStatic(DamageSource.IN_WALL, DamageSource.CRAMMING);
        distributionBuilderFactory.newEqualBuilder().reductionMultiplier(0.8F).registerDynamic(DamageSource::isExplosion);

        //---DEBUFFS---
        DebuffBuilderFactory debuffBuilderFactory = DebuffBuilderFactory.getInstance();
        loadValuesFromConfig(debuffBuilderFactory, "blindness", () -> FirstAidConfig.debuffs.head.blindness, EventHandler.HEARTBEAT, FirstAidConfig.debuffs.head.blindnessConditions, EnumDebuffSlot.HEAD);
        loadValuesFromConfig(debuffBuilderFactory, "nausea", () -> FirstAidConfig.debuffs.head.nausea, null, FirstAidConfig.debuffs.head.nauseaConditions, EnumDebuffSlot.HEAD);
        loadValuesFromConfig(debuffBuilderFactory, "nausea", () -> FirstAidConfig.debuffs.body.nausea, null, FirstAidConfig.debuffs.body.nauseaConditions, EnumDebuffSlot.BODY);
        loadValuesFromConfig(debuffBuilderFactory, "weakness", () -> FirstAidConfig.debuffs.body.weakness, FirstAidConfig.debuffs.body.weaknessConditions, EnumDebuffSlot.BODY);
        loadValuesFromConfig(debuffBuilderFactory, "mining_fatigue", () -> FirstAidConfig.debuffs.arms.mining_fatigue, FirstAidConfig.debuffs.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);
        loadValuesFromConfig(debuffBuilderFactory, "slowness", () -> FirstAidConfig.debuffs.legsAndFeet.slowness, FirstAidConfig.debuffs.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, SoundEvent event, FirstAidConfig.Debuffs.ConditionOnHit config, EnumDebuffSlot slot) {
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
        builder.register(slot);
    }

    private static void loadValuesFromConfig(DebuffBuilderFactory factory, String potionName, BooleanSupplier enableCondition, FirstAidConfig.Debuffs.ConditionConstant config, EnumDebuffSlot slot) {
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
