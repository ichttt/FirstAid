/*
 * FirstAid
 * Copyright (C) 2017-2023
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
import ichttt.mods.firstaid.api.debuff.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.event.RegisterDebuffEvent;
import ichttt.mods.firstaid.api.event.RegisterHealingTypeEvent;
import ichttt.mods.firstaid.common.RegistryObjects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class RegistryManager {
    public static final List<String> debuffConfigErrors = new ArrayList<>();

    public static void registerAndValidate() {
        MinecraftForge.EVENT_BUS.register(RegistryManager.class);
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

    public static void fireRegistryEvents(Level level) {
        if (FirstAidRegistry.getImpl() != null) FirstAid.LOGGER.warn("A registry has already been set!");
        RegisterDebuffEvent registerDebuffEvent = new RegisterDebuffEvent(level, new DebuffBuilderFactoryImpl());
        MinecraftForge.EVENT_BUS.post(registerDebuffEvent);
        RegisterHealingTypeEvent registerHealingTypeEvent = new RegisterHealingTypeEvent(level);
        MinecraftForge.EVENT_BUS.post(registerHealingTypeEvent);
        FirstAidRegistryImpl impl = new FirstAidRegistryImpl(registerHealingTypeEvent.getHealerMap(), registerDebuffEvent.getDebuffs());

        FirstAidRegistry.setImpl(impl);
    }

    public static void destroyRegistry() {
        if (FirstAidRegistry.getImpl() == null) FirstAid.LOGGER.warn("No registry has been set!");
        FirstAidRegistry.setImpl(null);
    }

//    @SubscribeEvent
//    public static void registerDamageDistributions(RegisterDamageDistributionEvent event) {
//        Level level = event.getLevel();
//        DamageDistributionBuilderFactory distributionBuilderFactory = event.getDistributionBuilderFactory();
//
//        DamageSources damageSources = level.damageSources();
//        event.registerDamageDistributionStatic(
//                distributionBuilderFactory.newStandardBuilder()
//                        .addDistributionLayer(EquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)
//                        .addDistributionLayer(EquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)
//                        .build(),
//                damageSources.fall(), damageSources.hotFloor());
//
//        event.registerDamageDistributionStatic(
//                distributionBuilderFactory.newStandardBuilder()
//                        .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
//                        .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM)
//                        .ignoreOrder()
//                        .build(),
//                damageSources.lightningBolt());
//
//        event.registerDamageDistributionStatic(
//                distributionBuilderFactory.newStandardBuilder()
//                        .addDistributionLayer(EquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)
//                        .build(),
//                damageSources.stalagmite());
//
//        event.registerDamageDistributionStatic(
//                distributionBuilderFactory.newStandardBuilder()
//                        .addDistributionLayer(EquipmentSlot.LEGS, EnumPlayerPart.RIGHT_LEG, EnumPlayerPart.LEFT_LEG)
//                        .addDistributionLayer(EquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)
//                        .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY)
//                        .build(),
//                damageSources.sweetBerryBush());
//
//        event.registerDamageDistributionStatic(distributionBuilderFactory.newRandomBuilder().build(), damageSources.magic());
//
//        if (FirstAidConfig.GENERAL.hardMode.get()) {
//            event.registerDamageDistributionStatic(
//                    distributionBuilderFactory.newStandardBuilder()
//                            .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.BODY)
//                            .disableNeighbourRestDistribution()
//                            .build(),
//                    damageSources.starve());
//
//            event.registerDamageDistributionStatic(
//                    distributionBuilderFactory.newStandardBuilder()
//                            .addDistributionLayer(EquipmentSlot.CHEST, EnumPlayerPart.BODY)
//                            .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
//                            .ignoreOrder()
//                            .disableNeighbourRestDistribution()
//                            .build(),
//                    damageSources.drown());
//        } else {
//            event.registerDamageDistributionStatic(distributionBuilderFactory.newRandomBuilder().tryNoKill().build(), damageSources.starve(), damageSources.drown());
//        }
//        event.registerDamageDistributionStatic(distributionBuilderFactory.newRandomBuilder().tryNoKill().build(), damageSources.inWall(), damageSources.cramming());
//        event.registerDamageDistributionDynamic(distributionBuilderFactory.newEqualBuilder().reductionMultiplier(0.8F).build(), damageSource -> damageSource.is(DamageTypeTags.IS_EXPLOSION));
//        event.registerDamageDistributionDynamic(
//                distributionBuilderFactory.newStandardBuilder()
//                        .addDistributionLayer(EquipmentSlot.HEAD, EnumPlayerPart.HEAD)
//                        .build(),
//                damageSource -> damageSource.typeHolder().is(DamageTypes.FALLING_ANVIL));
//    }

    @SubscribeEvent
    public static void registerDebuffs(RegisterDebuffEvent event) {
        loadValuesFromConfig(event, "blindness", RegistryObjects.HEARTBEAT, FirstAidConfig.GENERAL.head.blindnessConditions, EnumDebuffSlot.HEAD);
        loadValuesFromConfig(event, "nausea", null, FirstAidConfig.GENERAL.head.nauseaConditions, EnumDebuffSlot.HEAD);
        loadValuesFromConfig(event, "nausea", null, FirstAidConfig.GENERAL.body.nauseaConditions, EnumDebuffSlot.BODY);
        loadValuesFromConfig(event, "weakness", FirstAidConfig.GENERAL.body.weaknessConditions, EnumDebuffSlot.BODY);
        loadValuesFromConfig(event, "mining_fatigue", FirstAidConfig.GENERAL.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);
        loadValuesFromConfig(event, "slowness", FirstAidConfig.GENERAL.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
    }

    private static void loadValuesFromConfig(RegisterDebuffEvent event, String potionName, Supplier<SoundEvent> soundEventSupplier, FirstAidConfig.General.ConditionOnHit config, EnumDebuffSlot slot) {
        DebuffBuilderFactory debuffBuilderFactory = event.getDebuffBuilderFactory();
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

        IDebuffBuilder builder = debuffBuilderFactory.newOnHitDebuffBuilder(potionName);
        builder.addEnableCondition(config.enabled::get);
        for (int i = 0; i < damageTaken.length; i++)
            builder.addBound(damageTaken[i], debuffLength[i]);

        if (soundEventSupplier != null) builder.addSoundEffect(soundEventSupplier);
        event.registerDebuff(builder.build(), slot);
    }

    private static void loadValuesFromConfig(RegisterDebuffEvent event, String potionName, FirstAidConfig.General.ConditionConstant config, EnumDebuffSlot slot) {
        DebuffBuilderFactory debuffBuilderFactory = event.getDebuffBuilderFactory();
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
        IDebuffBuilder builder = debuffBuilderFactory.newConstantDebuffBuilder(potionName);
        builder.addEnableCondition(config.enabled::get);
        for (int i = 0; i < healthPercentageLeft.length; i++)
            builder.addBound(healthPercentageLeft[i], debuffStrength[i]);

        event.registerDebuff(builder.build(), slot);
    }

    private static void logError(String error, String potionName, EnumDebuffSlot slot) {
        String errorMsg = String.format("Invalid config entry for debuff %s at part %s: %s", potionName, slot.toString(), error);
        FirstAid.LOGGER.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }
}
