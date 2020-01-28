package ichttt.mods.firstaid.common.apiimpl.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionBuilder;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import net.minecraft.util.DamageSource;

import java.util.function.Predicate;

public abstract class BaseDamageDistributionBuilder implements IDamageDistributionBuilder {

    @Override
    public void registerDynamic(Predicate<DamageSource> matcher) {
        FirstAidRegistryImpl.INSTANCE.registerDistribution(matcher, build());
    }

    @Override
    public void registerStatic(DamageSource... source) {
        FirstAidRegistryImpl.INSTANCE.registerDistribution(source, build());
    }

    public abstract IDamageDistribution build();
}
