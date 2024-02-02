package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Map;

public class StaticDamageDistributionTarget implements IDamageDistributionTarget {
    private static final Codec<StaticDamageDistributionTarget> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    IDamageDistributionAlgorithm.DIRECT_CODEC.fieldOf("algorithm").forGetter(o -> o.algorithm),
                    ResourceLocation.CODEC.listOf().fieldOf("damageTypes").forGetter(o -> o.damageTypes)
            ).apply(instance, StaticDamageDistributionTarget::new)
    );

    private final IDamageDistributionAlgorithm algorithm;
    private final List<ResourceLocation> damageTypes;

    public StaticDamageDistributionTarget(IDamageDistributionAlgorithm algorithm, List<ResourceLocation> damageTypes) {
        this.algorithm = algorithm;
        this.damageTypes = damageTypes;
    }

    @Override
    public IDamageDistributionAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public List<DamageType> buildApplyList(IForgeRegistry<DamageType> allDamageTypes) {
        ImmutableList.Builder<DamageType> builder = ImmutableList.builder();
        for (Map.Entry<ResourceKey<DamageType>, DamageType> entry : allDamageTypes.getEntries()) {
            ResourceLocation location = entry.getKey().location();
            if (this.damageTypes.contains(location)) {
                builder.add(entry.getValue());
            }
        }
        return builder.build();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public Codec<? extends IDamageDistributionTarget> codec() {
        return CODEC;
    }
}
