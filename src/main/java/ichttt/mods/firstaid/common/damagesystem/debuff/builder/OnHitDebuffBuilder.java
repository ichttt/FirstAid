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

package ichttt.mods.firstaid.common.damagesystem.debuff.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.common.damagesystem.debuff.OnHitDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.OnHitDebuffEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Optional;

public class OnHitDebuffBuilder implements IDebuffBuilder {
    public static final Codec<OnHitDebuffBuilder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StringRepresentable.fromEnum(EnumDebuffSlot::values).fieldOf("debuffSlot").forGetter(o -> o.debuffSlot),
                    ResourceLocation.CODEC.fieldOf("potionName").forGetter(o -> o.effect),
                    OnHitDebuffEntry.CODEC.listOf().fieldOf("timeBoundaries").forGetter(o -> o.timeBoundaries),
                    ResourceLocation.CODEC.optionalFieldOf("soundEvent").forGetter(o -> o.sound == null ? Optional.empty() : Optional.of(o.sound))
            ).apply(instance, OnHitDebuffBuilder::new)
    );
    private final EnumDebuffSlot debuffSlot;
    private final ResourceLocation effect;
    private final List<OnHitDebuffEntry> timeBoundaries;
    private final ResourceLocation sound;

    public OnHitDebuffBuilder(EnumDebuffSlot debuffSlot, ResourceLocation effect, List<OnHitDebuffEntry> timeBoundaries, Optional<ResourceLocation> sound) {
        this.debuffSlot = debuffSlot;
        this.effect = effect;
        this.timeBoundaries = timeBoundaries;
        this.sound = sound.orElse(null);
    }

    @Override
    public Codec<? extends IDebuffBuilder> codec() {
        return CODEC;
    }

    @Override
    public EnumDebuffSlot affectedSlot() {
        return this.debuffSlot;
    }

    @Override
    public IDebuff build() {
        return new OnHitDebuff(this.effect, this.timeBoundaries, this.sound);
    }
}
