/*
 * FirstAid API
 * Copyright (c) 2017-2021
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
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The central registry for FirstAid.
 * <br>
 * Impl is set in PreInit, default values at init.
 * If you want to register your own values, you should probably do it in init.
 * If you want to override the default values, you should probably do it in PostInit.
 * <br>
 * <b>On LoadComplete event, all values should be present!</b>
 */
public abstract class FirstAidRegistry {
    @Nullable
    private static FirstAidRegistry instance;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setImpl(@Nonnull FirstAidRegistry registry) {
        instance = registry;
    }

    /**
     * Use this to get the active instance.
     * Null if FirstAid is not active or a version without this feature (prior to 1.3.2) is loaded
     */
    @Nullable
    public static FirstAidRegistry getImpl() {
        return instance;
    }

    /**
     * Registers your debuff to the FirstAid mod.
     *
     * @param slot    The slot this debuff should be active on
     * @param builder The builder containing all the information needed for the system.
     *                To retrieve a new builder use {@link ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory}
     */
    public abstract void registerDebuff(@Nonnull EnumDebuffSlot slot, @Nonnull IDebuffBuilder builder);

    /**
     * Registers you debuff to the FirstAid mod.
     * If you just need a simple onHit or constant debuff, you might want to use {@link #registerDebuff(EnumDebuffSlot, IDebuffBuilder)}
     *
     * @param slot   The slot this debuff should be active on
     * @param debuff Your custom implementation of a debuff
     */
    public abstract void registerDebuff(@Nonnull EnumDebuffSlot slot, @Nonnull Supplier<IDebuff> debuff);

    /**
     * Registers a healing type, so it can be used by the damage system when the user applies it.
     *
     * @param item      The item to bind to
     * @param factory   The factory to create a new healing type.
     *                  This should always create a new healer and get the itemstack that will shrink after creating the healer
     * @param applyTime The time it takes to apply this in the UI
     */
    public abstract void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, AbstractPartHealer> factory, Function<ItemStack, Integer> applyTime);

    @Nullable
    public abstract AbstractPartHealer getPartHealer(@Nonnull ItemStack type);

    public abstract Integer getPartHealingTime(@Nonnull ItemStack itemStack);

    @Deprecated // use nullable variant below, which returns null if nothing is registered instead of the default distribution
    @Nonnull
    public abstract IDamageDistribution getDamageDistribution(@Nonnull DamageSource source);

    @Nullable
    public abstract IDamageDistribution getDamageDistributionForSource(@Nonnull DamageSource source);

    @Nonnull
    public abstract IDebuff[] getDebuffs(@Nonnull EnumDebuffSlot slot);
}
