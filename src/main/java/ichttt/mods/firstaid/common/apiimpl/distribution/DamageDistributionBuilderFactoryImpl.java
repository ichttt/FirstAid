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
import ichttt.mods.firstaid.api.distribution.DamageDistributionBuilderFactory;
import ichttt.mods.firstaid.api.distribution.ICustomDamageDistributionBuilder;
import ichttt.mods.firstaid.api.distribution.IEqualDamageDistributionBuilder;
import ichttt.mods.firstaid.api.distribution.IRandomDamageDistributionBuilder;
import ichttt.mods.firstaid.api.distribution.IStandardDamageDistributionBuilder;

import javax.annotation.Nonnull;

public class DamageDistributionBuilderFactoryImpl extends DamageDistributionBuilderFactory {
    public static final DamageDistributionBuilderFactoryImpl INSTANCE = new DamageDistributionBuilderFactoryImpl();

    private DamageDistributionBuilderFactoryImpl() {}

    @Nonnull
    @Override
    public IStandardDamageDistributionBuilder newStandardBuilder() {
        return new StandardDamageDistributionBuilder();
    }

    @Nonnull
    @Override
    public IRandomDamageDistributionBuilder newRandomBuilder() {
        return new RandomDamageDistributionBuilder();
    }

    @Nonnull
    @Override
    public IEqualDamageDistributionBuilder newEqualBuilder() {
        return new EqualDamageDistributionBuilder();
    }

    @Nonnull
    @Override
    public ICustomDamageDistributionBuilder newCustomBuilder(IDamageDistribution distribution) {
        return new CustomDamageDistributionBuilder(distribution);
    }
}
