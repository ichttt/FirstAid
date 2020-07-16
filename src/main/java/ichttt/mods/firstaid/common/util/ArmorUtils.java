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
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
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
    public static double getArmor(ItemStack stack, EquipmentSlotType slot) {
        return getValueFromAttributes(Attributes.ARMOR, slot, stack);
    }

    public static double getArmorThoughness(ItemStack stack, EquipmentSlotType slot) {
        return getValueFromAttributes(Attributes.ARMOR_TOUGHNESS, slot, stack);
    }

    public static double applyArmorModifier(EquipmentSlotType slot, double rawArmor) {
        if (rawArmor <= 0D)
            return 0D;
        rawArmor = rawArmor * getArmorModifier(slot);
        if (slot == EquipmentSlotType.HEAD) rawArmor += 1F;
        return rawArmor;
    }

    public static double applyToughnessModifier(EquipmentSlotType slot, double rawToughness) {
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

    private static double getValueFromAttributes(Attribute attribute, EquipmentSlotType slot, ItemStack stack) {
        return stack.getItem().getAttributeModifiers(slot, stack).get(attribute).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    /**
     * Changed copy of ISpecialArmor {@link LivingEntity#applyArmorCalculations(DamageSource, float)}
     */
    @SuppressWarnings("JavadocReference")
    public static float applyArmor(@Nonnull PlayerEntity entity, @Nonnull ItemStack itemStack, @Nonnull DamageSource source, float damage, @Nonnull EquipmentSlotType slot) {
        if (itemStack.isEmpty() || source.isUnblockable()) return damage;
        Item item = itemStack.getItem();
        if (!(item instanceof ArmorItem)) return damage;
        ArmorItem armor = (ArmorItem) item;
        float totalArmor = armor.getDamageReduceAmount();
        float totalToughness = armor.func_234657_f_(); //getToughness
        totalArmor = (float) applyArmorModifier(slot, totalArmor);
        totalToughness = (float) applyToughnessModifier(slot, totalToughness);

        itemStack.damageItem((int) damage, entity, (player) -> player.sendBreakAnimation(slot));
        damage = CombatRules.getDamageAfterAbsorb(damage, totalArmor, totalToughness);
        return damage;
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
