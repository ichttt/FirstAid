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

import net.minecraft.item.ItemStack;

public abstract class AbstractPartHealer {
    public final int maxHeal;
    public final ItemStack stack;
    public final int ticksPerHeal;

    public AbstractPartHealer(int maxHeal, ItemStack stack, int ticksPerHeal) {
        this.maxHeal = maxHeal;
        this.stack = stack;
        this.ticksPerHeal = ticksPerHeal;
    }

    /**
     * Called when the part is loaded with saved data.
     *
     * @return this
     */
    public abstract AbstractPartHealer loadNBT(int ticksPassed, int heals);

    /**
     * Returns true if the healer is finished healing the body part.
     * The healer will be removed from the part at the next tick
     *
     * @return True if the healer is finished, otherwise false
     */
    public abstract boolean hasFinished();

    /**
     * Updates the healer.
     * Should not be called by other mods!
     */
    public abstract boolean tick();

    /**
     * Gets the time that passed since the
     */
    public abstract int getTicksPassed();

    /**
     * Gets the heals that this healer did
     */
    public abstract int getHealsDone();
}
