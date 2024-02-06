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

package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

public class TagDamageDistributionTarget implements IDamageDistributionTarget {

    public static final Codec<TagDamageDistributionTarget> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    IDamageDistributionAlgorithm.DIRECT_CODEC.fieldOf("algorithm").forGetter(o -> o.algorithm),
                    TagKey.codec(Registries.DAMAGE_TYPE).fieldOf("tag").forGetter(o -> o.tag)
            ).apply(instance, TagDamageDistributionTarget::new)
    );

    private final IDamageDistributionAlgorithm algorithm;
    private final TagKey<DamageType> tag;

    public TagDamageDistributionTarget(IDamageDistributionAlgorithm algorithm, TagKey<DamageType> tag) {
        this.algorithm = algorithm;
        this.tag = tag;
    }

    @Override
    public IDamageDistributionAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public List<DamageType> buildApplyList(Registry<DamageType> allDamageTypes) {
        ImmutableList.Builder<DamageType> builder = ImmutableList.builder();
        for (ResourceKey<DamageType> key : allDamageTypes.registryKeySet()) {
            allDamageTypes.getHolder(key).filter(holder -> holder.is(this.tag)).ifPresent(holder -> builder.add(holder.get()));
        }
        return builder.build();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public Codec<? extends IDamageDistributionTarget> codec() {
        return CODEC;
    }
}
