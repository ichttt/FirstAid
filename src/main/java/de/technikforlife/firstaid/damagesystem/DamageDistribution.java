package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import de.technikforlife.firstaid.network.MessageReceiveDamage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DamageDistribution {
    public static final DamageDistribution FALL_DMG = new DamageDistribution().addParts(EntityEquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT).addParts(EntityEquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG);
    public static final DamageDistribution HEAD = new DamageDistribution().addParts(EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD);
    public static final DamageDistribution STARVE = new DamageDistribution().addParts(EntityEquipmentSlot.CHEST, EnumPlayerPart.BODY);
    public static final DamageDistribution RANDOM_DIST = new RandomDamageDistribution();


    private final List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();

    protected DamageDistribution addParts(EntityEquipmentSlot slot, EnumPlayerPart... part) {
        partList.add(Pair.of(slot, part));
        return this;
    }

    private float distributeDamageOnParts(float damage, PlayerDamageModel damageModel, EnumPlayerPart[] enumParts, EntityPlayer player) {
        ArrayList<DamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (DamageablePart part : damageableParts) {
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part.part, damage), (EntityPlayerMP) player);
            float dmgDone = damage - part.damage(damage);
            if (dmgDone < 3.4028235E37F)
            {
                player.addStat(StatList.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
            }
            damage -= dmgDone;
            if (damage == 0)
                break;
            else if (damage < 0) {
                FirstAid.logger.error("Got negative damage {} left? Logic error? ", damage);
                break;
            }
        }
        return damage;
    }

    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        return partList;
    }

    public float distributeDamage(float damage, EntityPlayer player, DamageSource source) {
        PlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : getPartList()) {
            EntityEquipmentSlot slot = pair.getLeft();
            damage = ArmorUtils.applyArmor(player, player.getItemStackFromSlot(slot), source, damage, slot);
            if (damage <= 0F)
                return 0F;
            damage = ArmorUtils.applyEnchantmentModifieres(player.getItemStackFromSlot(slot), source, damage);
            if (damage <= 0F)
                return 0F;

            damage = distributeDamageOnParts(damage, damageModel, pair.getRight(), player);
            if (damage == 0F)
                break;
        }
        return damage;
    }


    private static final class RandomDamageDistribution extends DamageDistribution {
        private static final Map<EntityEquipmentSlot, List<EnumPlayerPart>> slotToParts = new HashMap<>();
        static {
            slotToParts.put(EntityEquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
            slotToParts.put(EntityEquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
            slotToParts.put(EntityEquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
            slotToParts.put(EntityEquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
        }

        @Override
        public DamageDistribution addParts(EntityEquipmentSlot slot, EnumPlayerPart... part) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
            List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
            List<EntityEquipmentSlot> slots = Arrays.asList(EntityEquipmentSlot.values());
            Collections.shuffle(slots);
            for (EntityEquipmentSlot slot : slots) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                List<EnumPlayerPart> parts = slotToParts.get(slot);
                Collections.shuffle(parts);
                partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
            }
            return partList;
        }
    }
}
