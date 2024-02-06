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
