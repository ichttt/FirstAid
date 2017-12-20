package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegistryManager {

    public static void setupRegistries() {
        FirstAidRegistry.setImpl(FirstAidRegistryImpl.INSTANCE);
        DebuffBuilderFactory.setInstance(DebuffBuilderFactoryImpl.INSTANCE);
    }

    public static void registerDefaults() {
        FirstAid.logger.debug("Registering defaults registry values");
        FirstAidRegistry registry = Objects.requireNonNull(FirstAidRegistry.getImpl());

        //---HEALING TYPES---
        registry.bindHealingType(EnumHealingType.BANDAGE, type -> new PartHealer(400, 3, type));
        registry.bindHealingType(EnumHealingType.PLASTER, type -> new PartHealer(500, 2, type));

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
        factory.newOnHitDebuffBuilder("blindness")
                .addSoundEffect(EventHandler.HEARTBEAT)
                .addEnableCondition(() -> FirstAidConfig.debuffs.head.blindness)
                .addBound(2F, 8 * 20)
                .addBound(1F, 4 * 20)
                .register(EnumDebuffSlot.HEAD);

        factory.newOnHitDebuffBuilder("nausea")
                .addEnableCondition(() -> FirstAidConfig.debuffs.head.nausea)
                .addBound(3F, 16 * 20)
                .addBound(2F, 12 * 20)
                .register(EnumDebuffSlot.HEAD);

        factory.newOnHitDebuffBuilder("nausea")
                .addEnableCondition(() -> FirstAidConfig.debuffs.body.nausea)
                .addBound(4F, 16 * 20)
                .addBound(2F, 8 * 20)
                .register(EnumDebuffSlot.BODY);

        factory.newConstantDebuffBuilder("weakness")
                .addEnableCondition(() -> FirstAidConfig.debuffs.body.weakness)
                .addBound(0.25F, 2)
                .addBound(0.50F, 1)
                .register(EnumDebuffSlot.BODY);

        factory.newConstantDebuffBuilder("mining_fatigue")
                .addEnableCondition(() -> FirstAidConfig.debuffs.arms.mining_fatigue)
                .addBound(0.25F, 3)
                .addBound(0.50F, 2)
                .addBound(0.75F, 1)
                .register(EnumDebuffSlot.ARMS);

        factory.newConstantDebuffBuilder("slowness")
                .addEnableCondition(() -> FirstAidConfig.debuffs.legsAndFeet.slowness)
                .addBound(0.35F, 3)
                .addBound(0.6F, 2)
                .addBound(0.85F, 1)
                .register(EnumDebuffSlot.LEGS_AND_FEET);
    }

    public static void finalizeRegistries() {
        FirstAidRegistryImpl.finish();
        DebuffBuilderFactoryImpl.verify();
    }
}
