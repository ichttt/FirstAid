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

package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;

import javax.annotation.Nonnull;

public class DebuffBuilderFactoryImpl extends DebuffBuilderFactory {
    public static final DebuffBuilderFactoryImpl INSTANCE = new DebuffBuilderFactoryImpl();

    public static void verify() {
        DebuffBuilderFactory registryImpl = DebuffBuilderFactory.getInstance();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
                    "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
    }

    /**
     * Creates a new builder for a onHit debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Override
    @Nonnull
    public IDebuffBuilder newOnHitDebuffBuilder(@Nonnull String potionName) {
        return new DebuffBuilder(potionName, true);
    }

    /**
     * Creates a new builder for a constant debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Override
    @Nonnull
    public IDebuffBuilder newConstantDebuffBuilder(@Nonnull String potionName) {
        return new DebuffBuilder(potionName, false);
    }
}
