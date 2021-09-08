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

package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import cpw.mods.modlauncher.TransformingClassLoader;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import org.apache.commons.lang3.ArrayUtils;

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
        DamageDistributionBuilderFactory.setInstance(DamageDistributionBuilderFactoryImpl.INSTANCE);
        //Validate everything is on the same TCL, otherwise things might break
        if (RegistryManager.class.getClassLoader() != FirstAidRegistry.class.getClassLoader()) {
            FirstAid.LOGGER.error("API and normal mod loaded on two different classloaders! Normal mod: {}, First Aid Registry: {}", RegistryManager.class.getName(), FirstAidRegistry.class.getName());
            throw new RuntimeException("API and normal mod loaded on two different classloaders!");
        }
        TransformingClassLoader tcl = (TransformingClassLoader) RegistryManager.class.getClassLoader();
        if (tcl.getLoadedClass(RegistryManager.class.getName()) != RegistryManager.class) {
            FirstAid.LOGGER.error("API is not the same as under tcl loaded classes! In TCL cache: {}, actual: {}", tcl.getLoadedClass(RegistryManager.class.getName()), RegistryManager.class);
            throw new RuntimeException("API is not under loaded classes in the TCL!");
        }
    }

    public static void registerDefaults() {
        FirstAid.LOGGER.debug("Registering defaults registry values");
        DamageDistributionBuilderFactory distributionBuilderFactory = Objects.requireNonNull(DamageDistributionBuilderFactory.getInstance());

        //---DAMAGE SOURCES---
        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)
                .addDistributionLayer(EquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)
                .registerStatic(DamageSource.FALL, DamageSource.HOT_FLOOR);

        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                .registerStatic(DamageSource.ANVIL);

        distributionBuilderFactory.newStandardBuilder()
                .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM)
                .ignoreOrder()
                .registerStatic(DamageSource.LIGHTNING_BOLT);

        distributionBuilderFactory.newRandomBuilder().registerStatic(DamageSource.MAGIC);

        if (FirstAidConfig.GENERAL.hardMode.get()) {
            distributionBuilderFactory.newStandardBuilder()
                    .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.BODY)
                    .disableNeighbourRestDistribution()
                    .registerStatic(DamageSource.STARVE);

            distributionBuilderFactory.newStandardBuilder()
                    .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.BODY)
                    .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
                    .ignoreOrder()
                    .disableNeighbourRestDistribution()
                    .registerStatic(DamageSource.DROWN);
        } else {
            distributionBuilderFactory.newRandomBuilder().tryNoKill().registerStatic(DamageSource.STARVE, DamageSource.DROWN);
        }
        distributionBuilderFactory.newRandomBuilder().tryNoKill().registerStatic(DamageSource.IN_WALL, DamageSource.CRAMMING);
        distributionBuilderFactory.newEqualBuilder().reductionMultiplier(0.8F).registerDynamic(DamageSource::isExplosion);

        //---DEBUFFS---
        DebuffBuilderFactory debuffBuilderFactory = DebuffBuilderFactory.getInstance();
        loadValuesFromConfig(debuffBuilderFactory, "blindness", () -> EventHandler.HEARTBEAT, FirstAidConfig.GENERAL.head.blindnessConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(debuffBuilderFactory, "nausea", null, FirstAidConfig.GENERAL.head.nauseaConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(debuffBuilderFactory, "nausea", null, FirstAidConfig.GENERAL.body.nauseaConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(debuffBuilderFactory, "weakness", FirstAidConfig.GENERAL.body.weaknessConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(debuffBuilderFactory, "mining_fatigue", FirstAidConfig.GENERAL.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);

        loadValuesFromConfig(debuffBuilderFactory, "slowness", FirstAidConfig.GENERAL.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
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
