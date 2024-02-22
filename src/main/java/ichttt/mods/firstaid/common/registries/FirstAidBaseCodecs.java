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

package ichttt.mods.firstaid.common.registries;

import com.mojang.serialization.Codec;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.minecraft.util.ExtraCodecs;

import java.util.function.Function;

public class FirstAidBaseCodecs {
    public static final Codec<IDebuffBuilder> DEBUFF_BUILDERS_DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> FirstAidRegistries.DEBUFF_BUILDERS.get().getCodec())
            .dispatch(IDebuffBuilder::codec, Function.identity());
    public static final Codec<IDamageDistributionAlgorithm> DAMAGE_DISTRIBUTION_ALGORITHMS_DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> FirstAidRegistries.DAMAGE_DISTRIBUTION_ALGORITHMS.get().getCodec())
            .dispatch(IDamageDistributionAlgorithm::codec, Function.identity());
    public static final Codec<IDamageDistributionTarget> DAMAGE_DISTRIBUTION_TARGETS_DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> FirstAidRegistries.DAMAGE_DISTRIBUTION_TARGETS.get().getCodec())
            .dispatch(IDamageDistributionTarget::codec, Function.identity());
}
