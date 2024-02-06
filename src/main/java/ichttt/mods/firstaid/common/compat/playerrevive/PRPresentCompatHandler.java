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

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import team.creative.playerrevive.api.IBleeding;

public class PRPresentCompatHandler implements IPRCompatHandler {
    private static final Capability<IBleeding> BLEEDING_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    static {
        FirstAid.LOGGER.info("Initializing PlayerRevive Compatibility...");
        if (BLEEDING_CAP == null) {
            FirstAid.LOGGER.warn("Failed to find player revive bleeding capability!");
        }
    }

    public static boolean canUse() {
        return BLEEDING_CAP != null;
    }


    /**
     * Gets the cap, or null if not applicable
     * @param player The player to check
     * @return The cap or null if the player cannot be revived
     */
    private static IBleeding getBleedingCapIfPossible(Player player) {
        if (player == null)
            return null;
        MinecraftServer server = player.getServer();
        if (server == null)
            return null;
        LazyOptional<IBleeding> bleeding = player.getCapability(BLEEDING_CAP);
        if (bleeding.isPresent() && server.isPublished())
            return bleeding.orElseThrow(RuntimeException::new);
        else
            return null;
    }

    @Override
    public boolean tryRevivePlayer(Player player, DamageSource source) {
        IBleeding bleedingCap = getBleedingCapIfPossible(player);
        if (bleedingCap != null) {
            bleedingCap.knockOut(player, source);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBleeding(Player player) {
        IBleeding revival = getBleedingCapIfPossible(player);
        if (revival != null) {
            return revival.isBleeding() && revival.timeLeft() > 0;
        }
        return false;
    }
}
