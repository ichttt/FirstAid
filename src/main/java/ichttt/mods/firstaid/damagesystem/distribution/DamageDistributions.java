package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;

public class DamageDistributions {
//    public static final DamageDistribution FALL_DMG = new StandardDamageDistribution().addParts(EntityEquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT).addParts(EntityEquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG);
//    public static final DamageDistribution HEAD = new StandardDamageDistribution().addParts(EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD);
//    public static final DamageDistribution STARVE = new StandardDamageDistribution().addParts(EntityEquipmentSlot.CHEST, EnumPlayerPart.BODY);
    public static final DamageDistribution FULL_RANDOM_DIST = new RandomDamageDistribution(false);
    public static final DamageDistribution SEMI_RANDOM_DIST = new RandomDamageDistribution(true);
}
