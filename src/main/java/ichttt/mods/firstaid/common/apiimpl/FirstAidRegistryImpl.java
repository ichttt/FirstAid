/*
 * FirstAid
 * Copyright (C) 2017-2023
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

import com.google.common.collect.Multimap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FirstAidRegistryImpl extends FirstAidRegistry {
    private final Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> healerMap;
    private final Multimap<EnumDebuffSlot, Supplier<IDebuff>> debuffs;

    public FirstAidRegistryImpl(Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> healerMap,
                                Multimap<EnumDebuffSlot, Supplier<IDebuff>> debuffs) {
        this.healerMap = healerMap;
        this.debuffs = debuffs;
    }

    @Nullable
    @Override
    public AbstractPartHealer getPartHealer(@Nonnull ItemStack type) {
        Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(type.getItem());
        if (pair != null)
            return pair.getLeft().apply(type);
        return null;
    }

    @Override
    public Integer getPartHealingTime(@Nonnull ItemStack stack) {
        Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(stack.getItem());
        if (pair != null)
            return pair.getRight().apply(stack);
        return null;
    }

    @Nullable
    @Override
    public IDamageDistributionAlgorithm getDamageDistributionForSource(@Nonnull DamageSource source) {
        Optional<ResourceKey<DamageType>> damageTypeResourceKeyOptional = source.typeHolder().unwrapKey();
        if (damageTypeResourceKeyOptional.isEmpty()) {
            FirstAid.LOGGER.warn("Attempted to get damage distrbution for unregistered damage source!");
            return null;
        }
        return null; //TODO
    }

    @Nonnull
    @Override
    public IDebuff[] getDebuffs(@Nonnull EnumDebuffSlot slot) {
        return debuffs.get(slot).stream().map(Supplier::get).toArray(IDebuff[]::new);
    }
}
