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

package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class DebuffBuilder implements IDebuffBuilder {
    @Nonnull
    public final String potionName;
    @Nonnull
    public final Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
    public final boolean isOnHit;
    @Nullable
    public SoundEvent sound;
    @Nullable
    public BooleanSupplier isEnabledSupplier;

    public DebuffBuilder(@Nonnull String potionName, boolean isOnHit) {
        this.potionName = Objects.requireNonNull(potionName);
        this.isOnHit = isOnHit;
    }

    @Override
    @Nonnull
    public DebuffBuilder addSoundEffect(@Nullable SoundEvent event) {
        this.sound = event;
        return this;
    }


    @Override
    @Nonnull
    public DebuffBuilder addBound(float value, int multiplier) {
        this.map.put(value, multiplier);
        return this;
    }

    @Override
    @Nonnull
    public DebuffBuilder addEnableCondition(@Nullable BooleanSupplier isEnabled) {
        this.isEnabledSupplier = isEnabled;
        return this;
    }

    @Override
    public void register(@Nonnull EnumDebuffSlot slot) {
        Objects.requireNonNull(FirstAidRegistry.getImpl()).registerDebuff(slot, this);
    }
}
