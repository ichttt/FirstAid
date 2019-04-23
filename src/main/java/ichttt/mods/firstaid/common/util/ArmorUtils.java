/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;

public class ArmorUtils {

    /**
     * Helper function to make {@link #applyArmor(EntityPlayer, ItemStack, DamageSource, float, EntityEquipmentSlot)}
     * more diffable
     */
    private static float getArmorModifier(EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST:
                return 2.5F;
            case LEGS:
                return 3F;
            case FEET:
            case HEAD:
                return 6.5F;
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
    }

    private static float getThougnessMofier(EntityEquipmentSlot slot) {
        return (slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS ? 3 : 4);
    }

    /**
     * Changed copy of ISpecialArmor {@link EntityLivingBase#applyArmorCalculations(DamageSource, float)}
     */
    @SuppressWarnings("JavadocReference")
    public static float applyArmor(@Nonnull EntityPlayer entity, @Nonnull ItemStack itemStack, @Nonnull DamageSource source, float damage, @Nonnull EntityEquipmentSlot slot) {
        if (itemStack.isEmpty() || source.isUnblockable()) return damage; //TODO validate
        Item item = itemStack.getItem();
        if (!(item instanceof ItemArmor)) return damage;
        ItemArmor armor = (ItemArmor) item;
        float totalArmor = armor.getDamageReduceAmount() * getArmorModifier(slot);
        float totalToughness = armor.getToughness() * getThougnessMofier(slot);

        itemStack.damageItem((int) damage, entity);
        damage = CombatRules.getDamageAfterAbsorb(damage, totalArmor, totalToughness);
        return damage;
    }

    /**
     * Changed copy of the first part from {@link EntityLivingBase#applyPotionDamageCalculations(DamageSource, float)}
     */
    @SuppressWarnings("JavadocReference")
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
     * Changed copy of the second part from {@link EntityLivingBase#applyPotionDamageCalculations(DamageSource, float)}
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
