/*
 * FirstAid API
 * Copyright (c) 2017-2019
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

import ichttt.mods.firstaid.api.damagesystem.PartHealer;
import ichttt.mods.firstaid.api.debuff.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumPlayerDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
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
     * Binds the damage source to a distribution.
     * The distribution will be a GeneralDamageDistribution
     *
     * @param damageType           The source - If you only have a source string, just wrap it in a {@link DamageSource(String)}
     * @param priorityList         The raw distribution table. Allows not as fine control, but can be used on all entities
     * @param shufflePriorityTable If the {@code priorityTable} should be shuffled before each distribution
     */
    public abstract void bindDamageSourceStandard(@Nonnull DamageSource damageType, boolean shufflePriorityTable, @Nonnull EntityEquipmentSlot... priorityList);

    /**
     * Binds the damage source to a distribution.
     * The distribution will be a StandardDamageDistribution or GeneralDamageDistribution
     *
     * @param damageType           The source - If you only have a source string, just wrap it in a {@link DamageSource(String)}
     * @param priorityTable        The fine-controlled distribution table. This will only be used for player entities
     * @param priorityList         The raw distribution table. Allows not as fine control, but can be used on all entities
     * @param shufflePriorityTable If the {@code priorityTable} should be shuffled before each distribution
     */
    public abstract void bindDamageSourceStandard(@Nonnull DamageSource damageType, boolean shufflePriorityTable, @Nullable List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable, @Nonnull EntityEquipmentSlot... priorityList);

    /**
     * Binds the damage source to a distribution.
     * The distribution will be a RandomDamageDistribution
     * This (with nearestFirst = true) is the default setting when nothing else is specified
     *
     * @param damageType   The source
     * @param nearestFirst True, if only a random start point should be chosen and the nearest other parts will be damaged
     *                     if the health there drops under zero, false if everything should be random
     * @param tryNoKill    If true, head and torso will only drop to 1 health and will only die if there is nothing else left
     */
    public abstract void bindDamageSourceRandom(@Nonnull DamageSource damageType, boolean nearestFirst, boolean tryNoKill);

    /**
     * Binds the damage source to a custom distribution
     *
     * @param damageType        The source
     * @param distributionTable Your custom distribution
     */
    public abstract void bindDamageSourceCustom(@Nonnull DamageSource damageType, @Nonnull IDamageDistribution distributionTable);

    /**
     * Registers your debuff to the FirstAid mod.
     *
     * @param playerSlot  The slot this debuff should be active on the player
     * @param generalSlot The slot this debuff should be active on general mobs. null if this only applies to players
     * @param builder     The builder containing all the information needed for the system.
     *                    To retrieve a new builder use {@link DebuffBuilderFactory}
     */
    public abstract void registerDebuff(@Nonnull EnumPlayerDebuffSlot playerSlot, @Nullable EntityEquipmentSlot generalSlot, @Nonnull IDebuffBuilder builder);

    /**
     * Registers you debuff to the FirstAid mod.
     * If you just need a simple onHit or constant debuff, you might want to use {@link #registerDebuff(EnumPlayerDebuffSlot, EntityEquipmentSlot, IDebuffBuilder)}
     *
     * @param playerSlot   The slot this debuff should be active on
     * @param generalSlot  The slot this debuff should be active on general mobs. null if this only applies to players
     * @param debuff       Your custom implementation of a debuff
     */
    public abstract void registerDebuff(@Nonnull EnumPlayerDebuffSlot playerSlot, @Nullable EntityEquipmentSlot generalSlot, @Nonnull Supplier<IDebuff> debuff);

    /**
     * Registers a healing type, so it can be used by the damage system when the user applies it.
     *
     * @param item      The item to bind to
     * @param factory   The factory to create a new healing type.
     *                  This should always create a new healer and get the itemstack that will shrink after creating the healer
     * @param applyTime The time it takes to apply this in the UI
     */
    public abstract void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, PartHealer> factory, Function<ItemStack, Integer> applyTime);

    @Nullable
    public abstract PartHealer getPartHealer(@Nonnull ItemStack type);

    public abstract Integer getPartHealingTime(@Nonnull ItemStack itemStack);

    @Nonnull
    public abstract IDamageDistribution getDamageDistribution(@Nonnull DamageSource source);

    @Nonnull
    public abstract IDebuff[] getPlayerDebuff(@Nonnull EnumPlayerDebuffSlot slot);

    @Nonnull
    public abstract IDebuff[] getGeneralDebuff(@Nonnull EntityEquipmentSlot slot, int count);
}
