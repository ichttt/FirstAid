package ichttt.mods.firstaid.api.distribution;

import javax.annotation.Nonnull;

public interface IRandomDamageDistributionBuilder extends IDamageDistributionBuilder {

    /**
     * If this is called, only a random start point should be chosen and the nearest other parts will be damaged
     * if the health in the affected limb drops under zero
     * @return The current builder
     */
    @Nonnull
    IRandomDamageDistributionBuilder useNearestFirst();

    /**
     * If this is called, the distribution will try to avoid killing people. If all non-critical health is depleted
     * and there is undistributed damage, the distribution will still kill the player to redistribute the missing damage.
     * @return The current builder
     */
    @Nonnull
    IRandomDamageDistributionBuilder tryNoKill();
}
