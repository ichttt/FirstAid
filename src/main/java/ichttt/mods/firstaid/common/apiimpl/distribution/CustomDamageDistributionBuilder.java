package ichttt.mods.firstaid.common.apiimpl.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.ICustomDamageDistributionBuilder;

public class CustomDamageDistributionBuilder extends BaseDamageDistributionBuilder implements ICustomDamageDistributionBuilder {
    private final IDamageDistribution distribution;

    public CustomDamageDistributionBuilder(IDamageDistribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public IDamageDistribution build() {
        return distribution;
    }
}
