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

package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.item.ItemHealing;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(FirstAid.MODID)
public class FirstAidItems {
    public static final Item BANDAGE = getNull();
    public static final Item PLASTER = getNull();
    public static final Item MORPHINE = getNull();

    @SuppressWarnings({"ConstantConditions", "SameReturnValue"})
    @Nonnull
    public static <T> T getNull() {
        return null;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        FirstAidConfig.Server server = FirstAidConfig.SERVER;
        registry.register(ItemHealing.create(new Item.Properties().stacksTo(16), new ResourceLocation(FirstAid.MODID, "bandage"), stack -> new PartHealer(() -> server.bandage.secondsPerHeal.get() * 20, server.bandage.totalHeals::get, stack), stack -> server.bandage.applyTime.get()));
        registry.register(ItemHealing.create(new Item.Properties().stacksTo(16), new ResourceLocation(FirstAid.MODID, "plaster"), stack -> new PartHealer(() -> server.plaster.secondsPerHeal.get() * 20, server.plaster.totalHeals::get, stack), stack -> server.plaster.applyTime.get()));
        registry.register(new ItemMorphine());
    }
}
