/*
 * FirstAid API
 * Copyright (c) 2017-2024
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.distribution;

import com.mojang.serialization.Codec;
import ichttt.mods.firstaid.common.registries.FirstAidRegistries;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;
import java.util.function.Function;

public interface IDamageDistributionTarget {
    Codec<IDamageDistributionTarget> DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> FirstAidRegistries.DAMAGE_DISTRIBUTION_TARGETS.get().getCodec())
            .dispatch(IDamageDistributionTarget::codec, Function.identity());

    IDamageDistributionAlgorithm getAlgorithm();

    List<DamageType> buildApplyList(Registry<DamageType> damageTypes);

    /**
     * Returns weather this target is dynamic of not.
     * Static takes precedence over dynamic, so if a static and dynamic target both specify an identical target, the static one will be used
     */
    boolean isDynamic();

    Codec<? extends IDamageDistributionTarget> codec();
}
