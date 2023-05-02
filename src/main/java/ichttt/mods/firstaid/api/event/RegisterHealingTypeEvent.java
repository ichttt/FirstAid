/*
 * FirstAid API
 * Copyright (c) 2017-2023
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

package ichttt.mods.firstaid.api.event;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RegisterHealingTypeEvent extends FirstAidRegisterEvent {
    private final Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> healerMap = new HashMap<>();

    public RegisterHealingTypeEvent(Level level) {
        super(level);
    }

    public void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, AbstractPartHealer> factory, Function<ItemStack, Integer> applyTime) {
        if (this.healerMap.containsKey(item))
            FirstAid.LOGGER.warn("Healing type override detected for item " + item);
        this.healerMap.put(item, Pair.of(factory, applyTime));
    }

    public Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> getHealerMap() {
        return Collections.unmodifiableMap(healerMap);
    }
}
