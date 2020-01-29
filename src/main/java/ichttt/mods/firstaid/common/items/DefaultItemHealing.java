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
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.item.ItemHealing;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class DefaultItemHealing extends ItemHealing {

    DefaultItemHealing(String name, Function<ItemStack, AbstractPartHealer> healerFunction, Function<ItemStack, Integer> applyTime) {
        super(healerFunction, applyTime);
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, name));
        setTranslationKey(name);
    }
}
