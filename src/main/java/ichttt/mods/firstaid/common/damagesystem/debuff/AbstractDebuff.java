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

package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class AbstractDebuff implements IDebuff {
    @Nonnull
    public final Potion effect;
    @Nonnull
    public final BooleanSupplier isEnabled;
    @Nonnull
    protected final Float2IntLinkedOpenHashMap map;

    public AbstractDebuff(@Nonnull String potionName, @Nonnull Float2IntLinkedOpenHashMap map, @Nonnull BooleanSupplier isEnabled) {
        this.effect = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionName)));
        this.isEnabled = isEnabled;
        this.map = map;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled.getAsBoolean();
    }
}
