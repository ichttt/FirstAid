package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DamageDistribution implements IDamageDistribution {

    public static void handleDamageTaken(IDamageDistribution damageDistribution, AbstractPlayerDamageModel damageModel, float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat, boolean redistributeIfLeft) {
        damage = ArmorUtils.applyGlobalPotionModifiers(player, source, damage);
        //VANILLA COPY - combat tracker and exhaustion
        if (damage != 0.0F) {
            player.addExhaustion(source.getHungerDamage());
            float currentHealth = player.getHealth();
            player.getCombatTracker().trackDamage(source, currentHealth, damage);
        }

        float left = damageDistribution.distributeDamage(damage, player, source, addStat);
        if (left > 0 && redistributeIfLeft) {
            damageDistribution = RandomDamageDistribution.NEAREST_KILL;
            damageDistribution.distributeDamage(left, player, source, addStat);
        }

        if (damageModel.isDead(player))
            CommonUtils.killPlayer(player, source);
    }

    protected float minHealth(@Nonnull EntityPlayer player, @Nonnull AbstractDamageablePart part) {
        return 0F;
    }

    protected float distributeDamageOnParts(float damage, @Nonnull AbstractPlayerDamageModel damageModel, @Nonnull EnumPlayerPart[] enumParts, @Nonnull EntityPlayer player, boolean addStat) {
        ArrayList<AbstractDamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (AbstractDamageablePart part : damageableParts) {
            float minHealth = minHealth(player, part);
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part.part, damage, minHealth), (EntityPlayerMP) player);
            float dmgDone = damage - part.damage(damage, player, damageModel.getMorphineTicks() == 0, minHealth);
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

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : getPartList()) {
            EntityEquipmentSlot slot = pair.getLeft();
            damage = ArmorUtils.applyArmor(player, player.getItemStackFromSlot(slot), source, damage, slot);
            if (damage <= 0F)
                return 0F;
            damage = ArmorUtils.applyEnchantmentModifiers(player.getItemStackFromSlot(slot), source, damage);
            if (damage <= 0F)
                return 0F;
            damage = ForgeHooks.onLivingDamage(player, source, damage); //we post every time we damage a part, make it so other mods can modify
            if (damage <= 0F) return 0F;

            damage = distributeDamageOnParts(damage, damageModel, pair.getRight(), player, addStat);
            if (damage == 0F)
                break;
        }
        return damage;
    }
}
