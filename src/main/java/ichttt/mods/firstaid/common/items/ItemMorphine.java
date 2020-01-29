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

package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemMorphine extends Item {

    public ItemMorphine() {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, "morphine"));
        setTranslationKey("morphine");
        setCreativeTab(FirstAid.CREATIVE_TAB);
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer && !(entityLiving instanceof FakePlayer)) {
            AbstractPlayerDamageModel damageModel = entityLiving.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
            Objects.requireNonNull(damageModel).applyMorphine((EntityPlayer) entityLiving);
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.EAT;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 40;
    }
}
