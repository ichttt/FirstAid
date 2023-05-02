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

package ichttt.mods.firstaid.api.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public abstract class DamageDistributionBuilderFactory {

    /**
     * Creates a new {@link IStandardDamageDistributionBuilder}.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a StandardDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IStandardDamageDistributionBuilder newStandardBuilder();

    /**
     * Creates a new {@link IRandomDamageDistributionBuilder}.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a RandomDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IRandomDamageDistributionBuilder newRandomBuilder();

    /**
     * Creates a new {@link IEqualDamageDistributionBuilder}, meaning all incoming damage will be split on all limbs
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a EqualDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IEqualDamageDistributionBuilder newEqualBuilder();

    /**
     * Creates a dummy builder for custom damage distribution implementation.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the distribution will be registered.
     * @return A new builder
     */
    @Nonnull
    public abstract ICustomDamageDistributionBuilder newCustomBuilder(IDamageDistribution distribution);
}
