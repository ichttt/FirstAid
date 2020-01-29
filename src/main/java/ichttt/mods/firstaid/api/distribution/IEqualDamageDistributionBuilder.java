package ichttt.mods.firstaid.api.distribution;

import javax.annotation.Nonnull;

public interface IEqualDamageDistributionBuilder extends IDamageDistributionBuilder {

    /**
     * If this is called, the distribution will try to avoid killing people. If all non-critical health is depleted
     * and there is undistributed damage, the distribution will still kill the player to redistribute the missing damage.
     * @return The current builder
     */
    @Nonnull
    IEqualDamageDistributionBuilder tryNoKill();

    /**
     * The amount that the reduction will be multiplied with.
     * Example: 10 input damage, reduced to 5 hearts. With multiplier=0.8, it would only be reduced to 4 hearts.
     * If no reduction takes place, the damage will stay the same
     * @param multiplier The multiplier for the reduction
     * @return The current builder
     */
    @Nonnull
    IEqualDamageDistributionBuilder reductionMultiplier(float multiplier);
}
