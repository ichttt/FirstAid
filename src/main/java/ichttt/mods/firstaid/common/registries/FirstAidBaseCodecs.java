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
