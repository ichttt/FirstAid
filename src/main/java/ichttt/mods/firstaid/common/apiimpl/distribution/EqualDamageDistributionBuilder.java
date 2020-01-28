package ichttt.mods.firstaid.common.apiimpl.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IEqualDamageDistributionBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistribution;

public class EqualDamageDistributionBuilder extends BaseDamageDistributionBuilder implements IEqualDamageDistributionBuilder {
    private boolean tryNoKill;

    @Override
    public IEqualDamageDistributionBuilder tryNoKill() {
        this.tryNoKill = true;
        return this;
    }

    @Override
    public IDamageDistribution build() {
        return new EqualDamageDistribution(tryNoKill);
    }
}
