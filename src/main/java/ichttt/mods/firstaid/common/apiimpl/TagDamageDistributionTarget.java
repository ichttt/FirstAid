package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.registries.IForgeRegistry;

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
    public List<DamageType> buildApplyList(IForgeRegistry<DamageType> allDamageTypes) {
        ImmutableList.Builder<DamageType> builder = ImmutableList.builder();
        for (DamageType damageType : allDamageTypes) {
            allDamageTypes.getHolder(damageType).filter(damageTypeHolder -> damageTypeHolder.is(this.tag)).ifPresent(holder -> builder.add(damageType));
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
