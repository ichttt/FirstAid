/*
 * FirstAid
 * Copyright (C) 2017-2020
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

package ichttt.mods.firstaid.common.util;

import com.google.common.collect.Iterators;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

import javax.annotation.Nonnull;

public class ArmorUtils {

    // Use attributes instead of fields on ItemArmor, these are likely more correct
    public static double getArmor(ItemStack stack, EntityEquipmentSlot slot) {
        return getValueFromAttributes(SharedMonsterAttributes.ARMOR, slot, stack);
    }

    public static double getArmorThoughness(ItemStack stack, EntityEquipmentSlot slot) {
        return getValueFromAttributes(SharedMonsterAttributes.ARMOR_TOUGHNESS, slot, stack);
    }

    public static double applyArmorModifier(EntityEquipmentSlot slot, double rawArmor) {
        if (rawArmor <= 0D)
            return 0D;
        rawArmor = rawArmor * getArmorModifier(slot);
        if (slot == EntityEquipmentSlot.HEAD) rawArmor += 1D;
        return rawArmor;
    }

    public static double applyToughnessModifier(EntityEquipmentSlot slot, double rawToughness) {
        if (rawToughness <= 0D)
            return 0D;
        rawToughness = rawToughness * getToughnessModifier(slot);
        return rawToughness;
    }

    private static double getArmorModifier(EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST:
                return 2.5D;
            case LEGS:
                return 3D;
            case FEET:
            case HEAD:
                return 6D;
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
    }

    private static double getToughnessModifier(EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST:
            case LEGS:
                return 3D;
            case FEET:
                return 3.5D;
            case HEAD:
                return 4D;
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
    }

    private static double getValueFromAttributes(IAttribute attribute, EntityEquipmentSlot slot, ItemStack stack) {
        return stack.getItem().getAttributeModifiers(slot, stack).get(attribute.getName()).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    /**
     * Changed copy of ISpecialArmor{@link ISpecialArmor.ArmorProperties#applyArmor(EntityLivingBase, NonNullList, DamageSource, double)}
     */
    public static float applyArmor(@Nonnull EntityPlayer entity, @Nonnull ItemStack itemStack, @Nonnull DamageSource source, double damage, @Nonnull EntityEquipmentSlot slot) {
        if (itemStack.isEmpty()) return (float)damage;
        NonNullList<ItemStack> inventory = entity.inventory.armorInventory;

        double totalArmor = 0;
        double totalToughness = 0;
        Item item = itemStack.getItem();

        ISpecialArmor.ArmorProperties prop;
        boolean unblockable = source.isUnblockable();
        if (item instanceof ISpecialArmor && (!unblockable || ((ISpecialArmor) item).handleUnblockableDamage(entity, itemStack, source, damage, slot.getIndex()))) {
            ISpecialArmor armor = (ISpecialArmor)item;
            prop = armor.getProperties(entity, itemStack, source, damage, slot.getIndex()).copy();
            totalArmor += prop.Armor;
            totalToughness += prop.Toughness;
        }  else if (item instanceof ItemArmor && !unblockable) {
            ItemArmor armor = (ItemArmor)item;
            prop = new ISpecialArmor.ArmorProperties(0, 0, Integer.MAX_VALUE);
            prop.Armor = armor.damageReduceAmount;
            prop.Toughness = armor.toughness;
        } else {
            return (float) damage;
        }
        if (item instanceof ItemArmor) { //Always add normal armor (even if the item is a special armor), as forge does this as well
            totalArmor += getArmor(itemStack, slot);
            totalToughness += getArmorThoughness(itemStack, slot);
        }

        totalArmor = applyArmorModifier(slot, totalArmor);
        totalToughness = applyToughnessModifier(slot, totalToughness);

        prop.Slot = slot.getIndex();
        double ratio = prop.AbsorbRatio * getArmorModifier(slot);

        double absorb = damage * ratio;
        if (absorb > 0) {
            ItemStack stack = inventory.get(prop.Slot);
            int itemDamage = (int) Math.max(1, absorb);
            if (stack.getItem() instanceof ISpecialArmor) ((ISpecialArmor) stack.getItem()).damageArmor(entity, stack, source, itemDamage, prop.Slot);
            else stack.damageItem(itemDamage, entity);
        }
        damage -= (damage * ratio);

        if (damage > 0 && (totalArmor > 0 || totalToughness > 0)) {
            double armorDamage = Math.max(1.0F, damage);

            if (item instanceof ItemArmor) itemStack.damageItem((int) armorDamage, entity);
            damage = CombatRules.getDamageAfterAbsorb((float)damage, (float)totalArmor, (float)totalToughness);
        }

        return (float)damage;
    }

    /**
     * Changed copy of the first part from {@link EnchantmentHelper#applyEnchantmentModifier(EnchantmentHelper.IModifier, ItemStack)}
     */
    public static float applyGlobalPotionModifiers(EntityPlayer player, DamageSource source, float damage) {
        if (source.isDamageAbsolute())
            return damage;
        if (player.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
            @SuppressWarnings("ConstantConditions")
            int i = (player.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float) j;
            damage = f / 25.0F;
        }

        if (damage <= 0.0F)
            return 0.0F;

        return damage;
    }

    /**
     * Changed copy of the second part from {@link EnchantmentHelper#applyEnchantmentModifier(EnchantmentHelper.IModifier, ItemStack)}
     */
    public static float applyEnchantmentModifiers(ItemStack stack, DamageSource source, float damage) {
        int k = EnchantmentHelper.getEnchantmentModifierDamage(() -> Iterators.singletonIterator(stack), source);
        k *= 4;

        if (k > 0)
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
        return damage;
    }
}
