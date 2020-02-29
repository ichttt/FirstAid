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

import javax.annotation.Nonnull;

public interface IEqualDamageDistributionBuilder extends IDamageDistributionBuilder {

    /**
     * If this is called, the distribution will try to avoid killing people. If all non-critical health is depleted
     * and there is undistributed damage, the distribution will still kill the player to redistribute the missing damage.
     * @return The current builder
     */
    @Nonnull
    IEqualDamageDistributionBuilder tryNoKill();

    /**
     * The amount that the reduction will be multiplied with.
     * Example: 10 input damage, reduced to 5 hearts. With multiplier=0.8, it would only be reduced to 4 hearts.
     * If no reduction takes place, the damage will stay the same
     * @param multiplier The multiplier for the reduction
     * @return The current builder
     */
    @Nonnull
    IEqualDamageDistributionBuilder reductionMultiplier(float multiplier);
}
