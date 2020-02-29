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
import ichttt.mods.firstaid.api.distribution.IDamageDistributionBuilder;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import net.minecraft.util.DamageSource;

import java.util.function.Predicate;

public abstract class BaseDamageDistributionBuilder implements IDamageDistributionBuilder {

    @Override
    public void registerDynamic(Predicate<DamageSource> matcher) {
        FirstAidRegistryImpl.INSTANCE.registerDistribution(matcher, build());
    }

    @Override
    public void registerStatic(DamageSource... source) {
        FirstAidRegistryImpl.INSTANCE.registerDistribution(source, build());
    }

    public abstract IDamageDistribution build();
}
