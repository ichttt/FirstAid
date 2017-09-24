package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.ArmorUtils;
import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import de.technikforlife.firstaid.network.MessageReceiveDamage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DamageDistribution {

    protected static float distributeDamageOnParts(float damage, PlayerDamageModel damageModel, EnumPlayerPart[] enumParts, EntityPlayer player, boolean addStat) {
        ArrayList<DamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (DamageablePart part : damageableParts) {
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part.part, damage), (EntityPlayerMP) player);
            float dmgDone = damage - part.damage(damage);
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
