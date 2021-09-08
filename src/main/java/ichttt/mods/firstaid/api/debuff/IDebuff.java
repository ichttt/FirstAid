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

package ichttt.mods.firstaid.api.debuff;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public interface IDebuff {

    void handleDamageTaken(float damage, float healthPerMax, ServerPlayer player);

    void handleHealing(float healingDone, float healthPerMax, ServerPlayer player);

    default boolean isEnabled() {
        return true;
    }

    default void update(Player player) {}

    default void update(Player player, float healthPerMax) {
        update(player);
    }
}
