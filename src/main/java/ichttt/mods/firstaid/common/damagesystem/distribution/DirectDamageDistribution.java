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

package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;

public class DirectDamageDistribution implements IDamageDistribution {
    private final EnumPlayerPart part;
    private final boolean debuff;

    public DirectDamageDistribution(EnumPlayerPart part, boolean debuff) {
        this.part = part;
        this.debuff = debuff;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        return player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null).getFromEnum(part).damage(damage, player, debuff);
    }
}
