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

package ichttt.mods.firstaid.api.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;

import javax.annotation.Nonnull;

public interface IStandardDamageDistributionBuilder extends IDamageDistributionBuilder {

    /**
     * Adds a new entry to the distribution table.
     * If {@link #ignoreOrder()} is called, the order of this doesn't matter, and a pair will be picked at random
     * Otherwise, the distribution will try to distribute the entire damage on the first registered pair,
     * if it can't distribute the entire damage there, the rest of the damage will be distributed to the
     * second pair and so on.
     * The slot has to match the parts
     * @return The current builder
     */
    @Nonnull
    IStandardDamageDistributionBuilder addDistributionLayer(EntityEquipmentSlot slot, EnumPlayerPart... parts);

    /**
     * If called, the distribution table will be shuffled, so a distribution pair is picked at random.
     * @return The current builder
     */
    @Nonnull
    IStandardDamageDistributionBuilder ignoreOrder();
}
