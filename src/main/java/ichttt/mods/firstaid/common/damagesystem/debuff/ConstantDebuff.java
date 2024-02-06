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

package ichttt.mods.firstaid.common.damagesystem.debuff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class ConstantDebuff extends AbstractDebuff {
    private int ticks = 0;
    private int activeMultiplier = 0;
    private final List<ConstantDebuffEntry> amplifierBoundaries;

    public ConstantDebuff(@Nonnull ResourceLocation potionName, @Nonnull List<ConstantDebuffEntry> amplifierBoundaries) {
        super(potionName);
        this.amplifierBoundaries = amplifierBoundaries;
    }

    private void syncMultiplier(float healthPerMax) {
        boolean found = false;
        for (ConstantDebuffEntry entry : amplifierBoundaries) {
            if (healthPerMax < entry.healthFractionThreshold()) {
                ticks = 0;
                activeMultiplier = entry.effectAmplifier(); // TODO move to zero-based numbering so we don't have to do - 1 later on.
                found = true;
                break;
            }
        }
        if (!found)
            activeMultiplier = 0;
    }

    @Override
    public void handleDamageTaken(float damage, float healthFraction, ServerPlayer player) {
        syncMultiplier(healthFraction);
    }

    @Override
    public void handleHealing(float healingDone, float healthFraction, ServerPlayer player) {
        syncMultiplier(healthFraction);
    }

    @Override
    public void update(Player player, float healthFraction) {
        if (activeMultiplier == 0) {
            ticks = 0;
            return;
        }

        if (ticks == 0) {
            if (healthFraction != -1) {
                syncMultiplier(healthFraction); //There are apparently some cases where the multiplier does not sync up right... fix this
            }
            if (activeMultiplier != 0) {
                player.addEffect(new MobEffectInstance(effect, 169, activeMultiplier - 1, false, false));
            }
        }
        ticks++;
        if (ticks >= 79) ticks = 0;
    }
}
