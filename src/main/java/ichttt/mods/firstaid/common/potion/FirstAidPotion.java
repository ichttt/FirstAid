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

package ichttt.mods.firstaid.common.potion;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirstAidPotion extends Effect {
    public FirstAidPotion(EffectType type, int liquidColorIn, Item item) {
        super(type, liquidColorIn);
        setRegistryName(new ResourceLocation(FirstAid.MODID, Objects.requireNonNull(item.getRegistryName()).getPath()));
    }

    @Nonnull
    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }
}
