/*
 * FirstAid API
 * Copyright (c) 2017-2020
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Methods that link to internal helper methods
 */
public abstract class HealingItemApiHelper {
    static HealingItemApiHelper INSTANCE;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setImpl(HealingItemApiHelper impl) {
        INSTANCE = impl;
    }

    @Nonnull
    public abstract ActionResult<ItemStack> onItemRightClick(ItemHealing itemHealing, World worldIn, EntityPlayer playerIn, EnumHand handIn);

    @Nonnull
    public abstract CreativeTabs getFirstAidTab();
}
