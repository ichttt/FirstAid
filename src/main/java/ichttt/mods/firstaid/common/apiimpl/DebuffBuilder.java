/*
 * FirstAid
 * Copyright (C) 2017-2022
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

import com.google.common.base.Preconditions;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.common.damagesystem.debuff.ConstantDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.OnHitDebuff;
import ichttt.mods.firstaid.common.util.CommonUtils;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DebuffBuilder implements IDebuffBuilder {
    @Nonnull
    public final String potionName;
    @Nonnull
    public final Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
    public final boolean isOnHit;
    @Nullable
    public Supplier<SoundEvent> sound;
    @Nullable
    public BooleanSupplier isEnabledSupplier;

    public DebuffBuilder(@Nonnull String potionName, boolean isOnHit) {
        this.potionName = Objects.requireNonNull(potionName);
        this.isOnHit = isOnHit;
    }

    @Override
    @Nonnull
    public DebuffBuilder addSoundEffect(@Nullable Supplier<SoundEvent> event) {
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
    public Supplier<IDebuff> build() {
        //Build the finished debuff
        FirstAid.LOGGER.debug("Building debuff from mod {} with potion effect {}, type = {}", CommonUtils.getActiveModidSafe(), this.potionName, this.isOnHit ? "OnHit" : "Constant");
        BooleanSupplier isEnabled;
        if (this.isEnabledSupplier == null)
            isEnabled = () -> true;
        else
            isEnabled = this.isEnabledSupplier;

        Preconditions.checkArgument(!this.map.isEmpty(), "Failed to register debuff with condition has set");
        Supplier<IDebuff> debuff;
        if (this.isOnHit) {
            debuff = () -> new OnHitDebuff(this.potionName, this.map, isEnabled, this.sound);
        } else {
            Preconditions.checkArgument(this.sound == null, "Tried to register constant debuff with sound effect.");
            debuff = () -> new ConstantDebuff(this.potionName, this.map, isEnabled);
        }

        return debuff;
    }
}
