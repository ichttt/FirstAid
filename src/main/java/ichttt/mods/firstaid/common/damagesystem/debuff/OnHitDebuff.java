/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class OnHitDebuff extends AbstractDebuff {
    @Nullable
    private final Supplier<SoundEvent> sound;

    public OnHitDebuff(@Nonnull String potionName, @Nonnull Float2IntLinkedOpenHashMap map, @Nonnull BooleanSupplier isEnabled, @Nullable Supplier<SoundEvent> sound) {
        super(potionName, map, isEnabled);
        this.sound = sound;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (!this.isEnabled.getAsBoolean())
            return;
        int value = -1;
        for (Float2IntMap.Entry entry : map.float2IntEntrySet()) {
            if (damage >= entry.getFloatKey()) {
                value = Math.max(value, entry.getIntValue());
                player.addPotionEffect(new PotionEffect(effect, entry.getIntValue(), 0, false, false));
            }
        }
        if (value != -1 && sound != null)
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessagePlayHurtSound(sound.get(), value));
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {

    }
}
