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
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(FirstAid.MODID)
public class FirstAidItems {
    public static final Item BANDAGE = getNull();
    public static final Item PLASTER = getNull();
    public static final Item MORPHINE = getNull();

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T> T getNull() {
        return null;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        FirstAidConfig.InternalHealing healing = FirstAidConfig.internalHealing;
        registry.register(new DefaultItemHealing("bandage", stack -> new PartHealer(healing.bandage.secondsPerHeal * 20, healing.bandage.totalHeals, stack), stack -> healing.bandage.applyTime));
        registry.register(new DefaultItemHealing("plaster", stack -> new PartHealer(healing.plaster.secondsPerHeal * 20, healing.plaster.totalHeals, stack), stack -> healing.plaster.applyTime));
        registry.register(new ItemMorphine());
    }
}
