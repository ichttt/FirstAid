/*
 * FirstAid API
 * Copyright (c) 2017-2023
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface IDamageDistributionAlgorithm {
    Codec<IDamageDistributionAlgorithm> DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> FirstAidRegistries.DAMAGE_DISTRIBUTION_ALGORITHMS.get().getCodec())
            .dispatch(IDamageDistributionAlgorithm::codec, Function.identity());

    float distributeDamage(float damage, @Nonnull Player player, @Nonnull DamageSource source, boolean addStat);

    default boolean skipGlobalPotionModifiers() {
        return false;
    }

    /**
     * @return the codec which serializes and deserializes this damage distribution
     */
    Codec<? extends IDamageDistributionAlgorithm> codec();
}
