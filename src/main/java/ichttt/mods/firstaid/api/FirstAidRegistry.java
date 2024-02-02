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

package ichttt.mods.firstaid.api;

import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The central registry for FirstAid.
 */
public abstract class FirstAidRegistry {
    @Nullable
    private static FirstAidRegistry instance;

    @ApiStatus.Internal
    public static void setImpl(@Nullable FirstAidRegistry registry) {
        instance = registry;
    }

    /**
     * Use this to get the active instance.
     * Null if the registry has not been loaded
     */
    @Nullable
    public static FirstAidRegistry getImpl() {
        return instance;
    }

    /**
     * Use this to get the active instance.
     * @throws IllegalStateException if the registry is not yet set
     */
    @Nonnull
    public static FirstAidRegistry getImplOrThrow() {
        FirstAidRegistry registry = instance;
        if (registry == null) throw new IllegalStateException();
        return registry;
    }

    @Nullable
    public abstract AbstractPartHealer getPartHealer(@Nonnull ItemStack type);

    public abstract Integer getPartHealingTime(@Nonnull ItemStack itemStack);

    @Nullable
    public abstract IDamageDistributionAlgorithm getDamageDistributionForSource(@Nonnull DamageSource source);

    @Nonnull
    public abstract IDebuff[] getDebuffs(@Nonnull EnumDebuffSlot slot);
}
