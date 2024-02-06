/*
 * FirstAid
 * Copyright (C) 2017-2024
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

package ichttt.mods.firstaid.common.compat.playerrevive;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class NoopPRCompatHandler implements IPRCompatHandler {

    @Override
    public boolean tryRevivePlayer(Player player, DamageSource source) {
        return false;
    }

    @Override
    public boolean isBleeding(Player player) {
        return false;
    }
}
