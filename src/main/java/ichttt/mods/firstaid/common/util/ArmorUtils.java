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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;

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
        if (slot == EquipmentSlotType.HEAD) rawArmor += 1F;
        return rawArmor;
    }

    public static float applyToughnessModifier(EquipmentSlotType slot, float rawToughness) {
        if (rawToughness <= 0F)
            return 0F;
        rawToughness = rawToughness * getToughnessModifier(slot);
        return rawToughness;
    }

    private static float getArmorModifier(EquipmentSlotType slot) {
        switch (slot) {
            case CHEST:
                return 2.5F;
            case LEGS:
                return 3F;
            case FEET:
            case HEAD:
                return 6F;
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
    }

    private static float getToughnessModifier(EquipmentSlotType slot) {
        switch (slot) {
            case CHEST:
            case LEGS:
                return 3F;
            case FEET:
                return 3.5F;
            case HEAD:
                return 4F;
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
    }

    private static double getValueFromAttributes(IAttribute attribute, EntityEquipmentSlot slot, ItemStack stack) {
        return stack.getItem().getAttributeModifiers(slot, stack).get(attribute.getName()).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    /**
     * Changed copy of ISpecialArmor {@link LivingEntity#applyArmorCalculations(DamageSource, float)}
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
     * Changed copy of the first part from {@link LivingEntity#applyPotionDamageCalculations(DamageSource, float)}
     */
    @SuppressWarnings("JavadocReference")
    public static float applyGlobalPotionModifiers(PlayerEntity player, DamageSource source, float damage) {
        if (source.isDamageAbsolute())
            return damage;
        if (player.isPotionActive(Effects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
            @SuppressWarnings("ConstantConditions")
            int i = (player.getActivePotionEffect(Effects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float) j;
            float f1 = damage;
            damage = Math.max(f / 25.0F, 0.0F);
            float f2 = f1 - damage;
            if (f2 > 0.0F && f2 < 3.4028235E37F) {
                if (player instanceof ServerPlayerEntity) {
                    player.addStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                } else if (source.getTrueSource() instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) source.getTrueSource()).addStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                }
            }
        }

        return damage;
    }

    /**
     * Changed copy of the second part from {@link LivingEntity#applyPotionDamageCalculations(DamageSource, float)}
     */
    @SuppressWarnings("JavadocReference")
    public static float applyEnchantmentModifiers(ItemStack stack, DamageSource source, float damage) {
        int k = EnchantmentHelper.getEnchantmentModifierDamage(() -> Iterators.singletonIterator(stack), source);
        k *= 4;

        if (k > 0)
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
        return damage;
    }
}
