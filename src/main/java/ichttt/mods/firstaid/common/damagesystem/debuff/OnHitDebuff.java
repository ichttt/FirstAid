/*
 * FirstAid
 * Copyright (C) 2017-2023
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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class OnHitDebuff extends AbstractDebuff {
    @Nonnull
    private final List<OnHitDebuffEntry> timeBoundaries;
    @Nullable
    private final SoundEvent sound;

    public OnHitDebuff(@Nonnull ResourceLocation potionName, @Nonnull List<OnHitDebuffEntry> timeBoundaries, @Nullable ResourceLocation soundEvent) {
        super(potionName);
        this.timeBoundaries = timeBoundaries;
        this.sound = soundEvent == null ? null : Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(soundEvent));
    }

    @Override
    public void handleDamageTaken(float damage, float healthFraction, ServerPlayer player) {
        int value = -1;
        for (OnHitDebuffEntry entry : timeBoundaries) {
            if (damage >= entry.damageTakenThreshold()) {
                value = Math.max(value, entry.effectDuration()); //TODO why is there no break here?
                player.addEffect(new MobEffectInstance(effect, entry.effectDuration(), 0, false, false));
            }
        }
        if (value != -1 && sound != null)
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessagePlayHurtSound(sound, value));
    }

    @Override
    public void handleHealing(float healingDone, float healthFraction, ServerPlayer player) {

    }
}
