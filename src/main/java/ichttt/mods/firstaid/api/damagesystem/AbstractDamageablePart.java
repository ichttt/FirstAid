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

package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractDamageablePart implements INBTSerializable<NBTTagCompound> {
    public final int initialMaxHealth;
    public final boolean canCauseDeath;
    @Nonnull
    public final EnumPlayerPart part;
    @Nullable
    public AbstractPartHealer activeHealer;
    public float currentHealth;

    public AbstractDamageablePart(int maxHealth, boolean canCauseDeath, @Nonnull EnumPlayerPart playerPart) {
        this.initialMaxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        this.part = playerPart;
    }

    /**
     * Heals the part for the specified amount.
     *
     * @param amount      The amount the part should be healed, clamped to max health
     * @param player      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the healing done
     * @return The amount of health that could not be added
     */
    public abstract float heal(float amount, @Nullable EntityPlayer player, boolean applyDebuff);

    /**
     * Damages the part for the specified amount.
     *
     * @param amount      The amount the part should be damaged, clamped to 0
     * @param player      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the damage taken
     * @return The amount of damage that could not be done
     */
    public abstract float damage(float amount, @Nullable EntityPlayer player, boolean applyDebuff);

    /**
     * Damages the part for the specified amount.
     *
     * @param amount      The amount the part should be damaged, clamped to minHealth
     * @param player      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the damage taken
     * @param minHealth   The minimum health the part should drop to
     * @return The amount of damage that could not be done
     */
    public abstract float damage(float amount, @Nullable EntityPlayer player, boolean applyDebuff, float minHealth);

    /**
     * Updates the part.
     * Should not be called by other mods!
     */
    public abstract void tick(World world, EntityPlayer player, boolean tickDebuffs);

    public abstract void setAbsorption(float absorption);

    public abstract float getAbsorption();

    public abstract void setMaxHealth(int maxHealth);

    public abstract int getMaxHealth();
}
