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

import com.google.common.base.Preconditions;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IEqualDamageDistributionBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistribution;

import javax.annotation.Nonnull;

public class EqualDamageDistributionBuilder extends BaseDamageDistributionBuilder implements IEqualDamageDistributionBuilder {
    private boolean tryNoKill;
    private float multiplier = 1F;

    @Nonnull
    @Override
    public IEqualDamageDistributionBuilder tryNoKill() {
        this.tryNoKill = true;
        return this;
    }

    @Nonnull
    @Override
    public IEqualDamageDistributionBuilder reductionMultiplier(float multiplier) {
        Preconditions.checkArgument(multiplier > 0, "reductionReduction must be greater than zero!");
        this.multiplier = multiplier;
        return this;
    }

    @Override
    public IDamageDistribution build() {
        return new EqualDamageDistribution(tryNoKill, multiplier);
    }
}
