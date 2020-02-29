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

package ichttt.mods.firstaid.common.apiimpl.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IStandardDamageDistributionBuilder;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class StandardDamageDistributionBuilder extends BaseDamageDistributionBuilder implements IStandardDamageDistributionBuilder {
    private final ArrayList<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
    private boolean ignoreOrder;

    @Nonnull
    @Override
    public IStandardDamageDistributionBuilder addDistributionLayer(EntityEquipmentSlot slot, EnumPlayerPart... parts) {
        partList.add(Pair.of(slot, parts));
        return this;
    }

    @Nonnull
    @Override
    public IStandardDamageDistributionBuilder ignoreOrder() {
        this.ignoreOrder = true;
        return this;
    }

    public IDamageDistribution build() {
        partList.trimToSize();
        if (partList.size() == 0) throw new IllegalArgumentException("Missing parts!");
        return new StandardDamageDistribution(partList, ignoreOrder);
    }
}
