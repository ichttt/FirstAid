/*
 * FirstAid
 * Copyright (C) 2017-2021
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

package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.item.HealingItemApiHelper;
import ichttt.mods.firstaid.api.item.ItemHealing;
import ichttt.mods.firstaid.client.ClientHooks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;

public class HealingItemApiHelperImpl extends HealingItemApiHelper {
    private static final HealingItemApiHelperImpl INSTANCE = new HealingItemApiHelperImpl();

    public static void init() {
        HealingItemApiHelper.setImpl(INSTANCE);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(ItemHealing itemHealing, Level world, Player player, InteractionHand hand) {
        if (world.isClientSide)
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiApplyHealth(hand));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Nonnull
    @Override
    public CreativeModeTab getFirstAidTab() {
        return FirstAid.ITEM_GROUP;
    }
}
