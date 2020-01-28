package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class EqualDamageDistribution implements IDamageDistribution {
    private static final Method applyPotionDamageCalculationsMethod = ObfuscationReflectionHelper.findMethod(EntityLivingBase.class, "func_70672_c", float.class, DamageSource.class, float.class);
    private final boolean tryNoKill;

    public EqualDamageDistribution(boolean tryNoKill) {
        this.tryNoKill = tryNoKill;
    }

    private float reduceDamage(float damage, EntityPlayer player, DamageSource source) {
        //As we damage all, also go through each armor slot
        for (EntityEquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
            ItemStack armor = player.getItemStackFromSlot(slot);
            damage = ArmorUtils.applyArmor(player, armor, source, damage, slot);
            if (damage <= 0F) return 0F;
        }
        //Use vanilla potion damage calculations
        try {
            damage = (float) applyPotionDamageCalculationsMethod.invoke(player, source, damage);
        } catch (IllegalAccessException | InvocationTargetException e) {
            FirstAid.LOGGER.error("Could not invoke applyPotionDamageCalculations!", e);
        }
        if (damage <= 0F) return 0F;
        damage = ForgeHooks.onLivingDamage(player, source, damage);
        return damage;
    }

    private float distributeOnParts(float damage, AbstractPlayerDamageModel damageModel, EntityPlayer player, boolean tryNoKillThisRound) {
        int iterationCounter = 0;
        int divCount = EnumPlayerPart.VALUES.length;
        float prevDamageLeft;
        float damageLeft = damage;
        do {
            //Setup values for next round
            prevDamageLeft = damageLeft;
            float toDamage = damageLeft / divCount;
            //Reset last counters
            divCount = 0;
            damageLeft = 0;

            for (AbstractDamageablePart part : damageModel) {
                if (part.currentHealth > 0F) {
                    damageLeft += part.damage(toDamage, player, player.isPotionActive(EventHandler.MORPHINE), tryNoKillThisRound ? 1F : 0F);
                    divCount++;
                }
            }

            //For safety
            if (iterationCounter >= 50) {
                FirstAid.LOGGER.warn("Not done distribution equally after 50 rounds, diff {}. Dropping!", Math.abs(prevDamageLeft - damageLeft));
                break;
            }
            iterationCounter++;
        } while (prevDamageLeft != damageLeft);
        return damageLeft;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        damage = reduceDamage(damage, player, source);
        if (damage <= 0F) return 0F;
        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        Objects.requireNonNull(damageModel);

        float damageLeft = distributeOnParts(damage, damageModel, player, tryNoKill);
        if (damageLeft > 0F && tryNoKill)
            damageLeft = distributeOnParts(damage, damageModel, player, false);

        float effectiveDmg = damage - damageLeft;
        if (effectiveDmg < 3.4028235E37F) {
            player.addStat(StatList.DAMAGE_TAKEN, Math.round(effectiveDmg * 10.0F));
        }
        return damageLeft;
    }
}
