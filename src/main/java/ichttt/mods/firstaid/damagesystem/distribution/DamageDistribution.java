package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.util.ArmorUtils;
import ichttt.mods.firstaid.damagesystem.DamageablePart;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;
import ichttt.mods.firstaid.network.MessageReceiveDamage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DamageDistribution {
    protected static final EntityEquipmentSlot[] ARMOR_SLOTS;
    protected static final Map<EntityEquipmentSlot, List<EnumPlayerPart>> slotToParts = new HashMap<>();

    static {
        ARMOR_SLOTS = new EntityEquipmentSlot[4];
        ARMOR_SLOTS[3] = EntityEquipmentSlot.HEAD;
        ARMOR_SLOTS[2] = EntityEquipmentSlot.CHEST;
        ARMOR_SLOTS[1] = EntityEquipmentSlot.LEGS;
        ARMOR_SLOTS[0] = EntityEquipmentSlot.FEET;
        slotToParts.put(EntityEquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
        slotToParts.put(EntityEquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
        slotToParts.put(EntityEquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
        slotToParts.put(EntityEquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
    }

    protected static float distributeDamageOnParts(float damage, PlayerDamageModel damageModel, EnumPlayerPart[] enumParts, EntityPlayer player, boolean addStat) {
        ArrayList<DamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (DamageablePart part : damageableParts) {
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part.part, damage), (EntityPlayerMP) player);
            float dmgDone = damage - part.damage(damage, player, damageModel.getMorphineTicks() == 0);
            if (addStat)
                player.addStat(StatList.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
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

    @Nonnull
    protected abstract List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList();

    public float distributeDamage(float damage, EntityPlayer player, DamageSource source, boolean addStat) {
        PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : getPartList()) {
            EntityEquipmentSlot slot = pair.getLeft();
            damage = ArmorUtils.applyArmor(player, player.getItemStackFromSlot(slot), source, damage, slot);
            if (damage <= 0F)
                return 0F;
            damage = ArmorUtils.applyEnchantmentModifieres(player.getItemStackFromSlot(slot), source, damage);
            if (damage <= 0F)
                return 0F;

            damage = distributeDamageOnParts(damage, damageModel, pair.getRight(), player, addStat);
            if (damage == 0F)
                break;
        }
        return damage;
    }
}
