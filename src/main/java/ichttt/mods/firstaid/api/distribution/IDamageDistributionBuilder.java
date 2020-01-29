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

import net.minecraft.util.DamageSource;

import java.util.function.Predicate;

public interface IDamageDistributionBuilder {

    /**
     * Binds a matcher predicate.
     * The matcher should be simple, as it will be called every time no static fitting distribution could be found.
     * Use {@link #registerStatic(DamageSource[])} whenever possible, as it is the faster option
     * @param matcher The matcher to select whether this distribution should be used or not.
     *                Will only be called if no static distribution could be found
     */
    void registerDynamic(Predicate<DamageSource> matcher);

    /**
     * Binds the damage source to a distribution.
     * This should be preferred over {@link #registerDynamic(Predicate)} whenever possible
     * @param source The sources that should use this distribution
     */
    void registerStatic(DamageSource... source);
}
