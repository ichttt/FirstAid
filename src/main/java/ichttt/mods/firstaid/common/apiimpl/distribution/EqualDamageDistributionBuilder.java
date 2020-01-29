package ichttt.mods.firstaid.common.apiimpl.distribution;

import com.google.common.base.Preconditions;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IEqualDamageDistributionBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistribution;

import javax.annotation.Nonnull;

public class EqualDamageDistributionBuilder extends BaseDamageDistributionBuilder implements IEqualDamageDistributionBuilder {
    private boolean tryNoKill;
    private float multiplier = 1F;

    @Nonnull
    @Override
    public IEqualDamageDistributionBuilder tryNoKill() {
        this.tryNoKill = true;
        return this;
    }

    @Nonnull
    @Override
    public IEqualDamageDistributionBuilder reductionMultiplier(float multiplier) {
        Preconditions.checkArgument(multiplier > 0, "reductionReduction must be greater than zero!");
        this.multiplier = multiplier;
        return this;
    }

    @Override
    public IDamageDistribution build() {
        return new EqualDamageDistribution(tryNoKill, multiplier);
    }
}
