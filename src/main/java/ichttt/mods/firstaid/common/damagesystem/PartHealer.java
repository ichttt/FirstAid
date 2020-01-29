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

package ichttt.mods.firstaid.common.damagesystem;

import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.item.ItemStack;

public class PartHealer extends AbstractPartHealer {
    private int ticksPassed = 0;
    private int heals = 0;

    public PartHealer(int ticksPerHeal, int maxHeal, ItemStack stack) {
        super(maxHeal, stack, ticksPerHeal);
    }

    @Override
    public AbstractPartHealer loadNBT(int ticksPassed, int heals) {
        this.ticksPassed = ticksPassed;
        this.heals = heals;
        return this;
    }

    @Override
    public boolean hasFinished() {
        return heals >= maxHeal;
    }

    @Override
    public boolean tick() {
        if (hasFinished())
            return false;
        ticksPassed++;
        boolean nextHeal = ticksPassed >= ticksPerHeal;
        if (nextHeal) {
            ticksPassed = 0;
            heals++;
        }
        return nextHeal;
    }

    @Override
    public int getTicksPassed() {
        return ticksPassed;
    }

    @Override
    public int getHealsDone() {
        return heals;
    }
}
