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

package ichttt.mods.firstaid.api.debuff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IDebuff {

    /**
     * Called when the {@link ichttt.mods.firstaid.common.damagesystem.DamageablePart} this debuff belongs to has taken damage.
     * For debuffs that apply to multiple {@link ichttt.mods.firstaid.api.enums.EnumPlayerPart}, {@link ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff} will do additional calculations and call this method
     * @param damage The damage taken in this hit
     * @param healthFraction The fraction of current health to max health of the corresponding part(s)
     * @param player The player that may be affected
     */
    void handleDamageTaken(float damage, float healthFraction, ServerPlayer player);

    /**
     * Called when the {@link ichttt.mods.firstaid.common.damagesystem.DamageablePart} this debuff belongs to has been healed.
     * For debuffs that apply to multiple {@link ichttt.mods.firstaid.api.enums.EnumPlayerPart}, {@link ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff} will do additional calculations and call this method
     * @param healingDone The healing done
     * @param healthFraction The fraction of current health to max health of the corresponding part(s)
     * @param player The player that may be affected
     */
    void handleHealing(float healingDone, float healthFraction, ServerPlayer player);

    /**
     * Called each this
     * @param player The player that may be affected
     * @param healthFraction The fraction of current health to max health of the corresponding part(s)
     */
    default void update(Player player, float healthFraction) {}
}
