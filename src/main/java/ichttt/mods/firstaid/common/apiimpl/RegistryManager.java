package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import net.minecraft.inventory.EntityEquipmentSlot;
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
        FirstAid.logger.debug("Registering defaults registry values");
        FirstAidRegistry registry = Objects.requireNonNull(FirstAidRegistry.getImpl());

        //---HEALING TYPES---
        registry.registerHealingType(FirstAidItems.BANDAGE, stack -> new PartHealer(18 * 20, 4, stack));
        registry.registerHealingType(FirstAidItems.PLASTER, stack -> new PartHealer(22 * 20, 2, stack));

        //---DAMAGE SOURCES---
        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> feetList = new ArrayList<>(2);
        feetList.add(Pair.of(EntityEquipmentSlot.FEET, new EnumPlayerPart[]{EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT}));
        feetList.add(Pair.of(EntityEquipmentSlot.LEGS, new EnumPlayerPart[]{EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG}));
        registry.bindDamageSourceStandard("fall", feetList);
        registry.bindDamageSourceStandard("hotFloor", feetList);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> headList = new ArrayList<>(1);
        headList.add(Pair.of(EntityEquipmentSlot.HEAD, new EnumPlayerPart[]{EnumPlayerPart.HEAD}));
        registry.bindDamageSourceStandard("anvil", headList);

        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> bodyList = new ArrayList<>(1);
        bodyList.add(Pair.of(EntityEquipmentSlot.CHEST, new EnumPlayerPart[]{EnumPlayerPart.BODY}));
        registry.bindDamageSourceStandard("starve", bodyList);

        registry.bindDamageSourceRandom("magic", false, false);
        registry.bindDamageSourceRandom("drown", false, true);
        registry.bindDamageSourceRandom("inWall", false, true);

        //---DEBUFFS---
        DebuffBuilderFactory factory = DebuffBuilderFactory.getInstance();
        loadValuesFromConfig(factory, "blindness", () -> FirstAidConfig.debuffs.head.blindness, EventHandler.HEARTBEAT, FirstAidConfig.debuffs.head.blindnessConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(factory, "nausea", () -> FirstAidConfig.debuffs.head.nausea, null, FirstAidConfig.debuffs.head.nauseaConditions, EnumDebuffSlot.HEAD);

        loadValuesFromConfig(factory, "nausea", () -> FirstAidConfig.debuffs.body.nausea, null, FirstAidConfig.debuffs.body.nauseaConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(factory, "weakness", () -> FirstAidConfig.debuffs.body.weakness, FirstAidConfig.debuffs.body.weaknessConditions, EnumDebuffSlot.BODY);

        loadValuesFromConfig(factory, "mining_fatigue", () -> FirstAidConfig.debuffs.arms.mining_fatigue, FirstAidConfig.debuffs.arms.miningFatigueConditions, EnumDebuffSlot.ARMS);

        loadValuesFromConfig(factory, "slowness", () -> FirstAidConfig.debuffs.legsAndFeet.slowness, FirstAidConfig.debuffs.legsAndFeet.slownessConditions, EnumDebuffSlot.LEGS_AND_FEET);
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
        FirstAid.logger.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }

    public static void finalizeRegistries() {
        FirstAidRegistryImpl.finish();
        DebuffBuilderFactoryImpl.verify();
    }
}
